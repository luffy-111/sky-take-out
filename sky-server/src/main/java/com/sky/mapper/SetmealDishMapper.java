package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 批量插入套餐菜品数据
     *
     * @param setmealDishes
     */
    @AutoFill(value = OperationType.INSERT)
    void insertSetmealDish(@Param("setmealDishes") List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id删除套餐菜品关系
     *
     * @param ids
     * @return
     */
    void deleteBySetmealIds(List<Long> ids);

    /**
     * 根据套餐id查询套餐菜品关系
     *
     * @param id
     * @return
     */
    List<SetmealDish> getBySetmealId(Long id);

    /**
     * 根据套餐id查询菜品选项
     *
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);
}
