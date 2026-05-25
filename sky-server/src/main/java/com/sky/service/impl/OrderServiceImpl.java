package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        // 处理各种异常情况(地址簿为空, 购物车为空)
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        // 向订单明细表插入n条数据
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();  // 订单明细
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());  // 订单id
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        // 下单成功后, 清空购物车数据
        shoppingCartMapper.deleteByUserId(userId);  // 清空购物车

        // 返回OrderSubmitVO
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
    }

    /**
     * 订单支付 - 模拟支付，跳过微信支付
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        // 直接调用支付成功逻辑，更新订单状态
        paySuccess(ordersPaymentDTO.getOrderNumber());

        // 返回空的 VO，前端收到后会跳转支付成功页
        return new OrderPaymentVO();
    }

    /**
     * 历史订单查询
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        // 设置分页参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> list = new ArrayList<>();
        if (page != null && page.getTotal() > 0) {
            for (Orders orders : page) {
                list.add(buildOrderVO(orders));
            }
        }
        return new PageResult(page.getTotal(), list);
    }

    /**
     * 管理端订单搜索
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> list = new ArrayList<>();
        if (page != null && page.getTotal() > 0) {
            for (Orders orders : page) {
                list.add(buildOrderVO(orders));
            }
        }
        return new PageResult(page.getTotal(), list);
    }

    /**
     * 各状态订单数量统计
     */
    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(orderMapper.countByStatus(Orders.CONFIRMED));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS));
        orderStatisticsVO.setToBeConfirmed(orderMapper.countByStatus(Orders.TO_BE_CONFIRMED));
        return orderStatisticsVO;
    }

    /**
     * 查询订单详情
     */
    @Override
    public OrderVO getOrderDetail(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null || !BaseContext.getCurrentId().equals(orders.getUserId())) {
            return null;
        }

        return buildOrderVO(orders);
    }

    /**
     * 管理端查询订单详情
     */
    @Override
    public OrderVO adminOrderDetail(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            return null;
        }
        return buildOrderVO(orders);
    }

    /**
     * 取消订单
     */
    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null || !BaseContext.getCurrentId().equals(orders.getUserId())) {
            return;
        }

        if (!(Orders.PENDING_PAYMENT.equals(orders.getStatus()) || Orders.TO_BE_CONFIRMED.equals(orders.getStatus()))) {
            throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders update = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .cancelReason("用户取消订单")
                .build();
        orderMapper.update(update);
    }

    /**
     * 再来一单
     */
    @Override
    @Transactional
    public void repetition(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null || !BaseContext.getCurrentId().equals(orders.getUserId())) {
            return;
        }

        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        if (orderDetailList == null || orderDetailList.isEmpty()) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }

        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setId(null);
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    private OrderVO buildOrderVO(Orders orders) {
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);

        AddressBook addressBook = addressBookMapper.getById(orders.getAddressBookId());
        if (addressBook != null) {
            orderVO.setAddress(addressBook.getProvinceName() + addressBook.getCityName()
                    + addressBook.getDistrictName() + addressBook.getDetail());
        }

        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        orderVO.setOrderDetailList(orderDetailList);
        orderVO.setOrderDishes(buildOrderDishes(orderDetailList));
        return orderVO;
    }

    private String buildOrderDishes(List<OrderDetail> orderDetailList) {
        if (orderDetailList == null || orderDetailList.isEmpty()) {
            return "";
        }
        return orderDetailList.stream()
                .map(orderDetail -> orderDetail.getName() + " x" + orderDetail.getNumber())
                .collect(Collectors.joining("; "));
    }

    /**
     * 支付成功，修改订单状态
     */
    private void paySuccess(String outTradeNo) {
        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByOrderNumber(outTradeNo);

        // 更新订单状态：待付款 -> 待接单，支付状态 -> 已支付
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)   // 待接单
                .payStatus(Orders.PAID)            // 已支付
                .checkoutTime(LocalDateTime.now()) // 结账时间
                .build();

        orderMapper.update(orders);
    }
}

