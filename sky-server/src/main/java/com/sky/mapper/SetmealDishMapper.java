package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
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
     * 根据套餐id查询套餐菜品关系
     *
     * @param ids
     * @return
     */
    void deleteBySetmealIds(List<Long> ids);
}
