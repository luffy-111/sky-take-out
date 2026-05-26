package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<LocalDate> dateList = new ArrayList<>();
        List<Double> turnoverList = new ArrayList<>();
        Map<String, Double> turnoverMap = orderMapper.getTurnoverStatistics(beginTime, endTime)
                .stream()
                .collect(Collectors.toMap(
                        item -> (String) item.get("statDate").toString(),
                        item -> ((Number) item.get("totalAmount")).doubleValue()
                ));

        while (!begin.equals(end)) {
            dateList.add(begin);
            String key = begin.toString();
            turnoverList.add(turnoverMap.getOrDefault(key, 0.0));
            begin = begin.plusDays(1);
        }
        dateList.add(end);
        turnoverList.add(turnoverMap.getOrDefault(end.toString(), 0.0));

        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        List<Map<String, Object>> userStatistics = userMapper.getUserStatistics(beginTime, endTime);
        Map<String, Map<String, Object>> userStatisticsMap = userStatistics.stream()
                .collect(Collectors.toMap(
                        item -> item.get("statDate").toString(),
                        item -> item
                ));

        int totalUser = userStatistics.isEmpty() ? 0 : ((Number) userStatistics.get(0).get("totalUser")).intValue();
        while (!begin.equals(end)) {
            dateList.add(begin);
            Map<String, Object> stat = userStatisticsMap.get(begin.toString());
            int newUser = stat == null ? 0 : ((Number) stat.get("userCount")).intValue();
            newUserList.add(newUser);
            totalUserList.add(totalUser);
            begin = begin.plusDays(1);
        }
        dateList.add(end);
        Map<String, Object> stat = userStatisticsMap.get(end.toString());
        int newUser = stat == null ? 0 : ((Number) stat.get("userCount")).intValue();
        newUserList.add(newUser);
        totalUserList.add(totalUser);

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        Map<String, Map<String, Object>> orderStatisticsMap = orderMapper.getOrderStatistics(beginTime, endTime)
                .stream()
                .collect(Collectors.toMap(
                        item -> item.get("statDate").toString(),
                        item -> item
                ));

        while (!begin.equals(end)) {
            dateList.add(begin);
            Map<String, Object> stat = orderStatisticsMap.get(begin.toString());
            int orderCount = stat == null ? 0 : ((Number) stat.get("orderCount")).intValue();
            int validOrderCount = stat == null || stat.get("validOrderCount") == null ? 0 : ((Number) stat.get("validOrderCount")).intValue();
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
            begin = begin.plusDays(1);
        }
        dateList.add(end);
        Map<String, Object> stat = orderStatisticsMap.get(end.toString());
        int orderCount = stat == null ? 0 : ((Number) stat.get("orderCount")).intValue();
        int validOrderCount = stat == null || stat.get("validOrderCount") == null ? 0 : ((Number) stat.get("validOrderCount")).intValue();
        orderCountList.add(orderCount);
        validOrderCountList.add(validOrderCount);

        // 计算时间区间内的订单总数
        int totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        // 计算时间区间内的有效订单总数
        Integer validOrderTotal = validOrderCountList.stream().reduce(Integer::sum).get();
        // 计算订单完成率
        double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderTotal.doubleValue() / totalOrderCount;
        }

        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderTotal)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 查询Top10
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        // 封装返回结果
        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(names, ","))
                .numberList(StringUtils.join(numbers, ","))
                .build();
    }

}
