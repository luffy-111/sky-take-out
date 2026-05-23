package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 *
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        // 清理缓存数据
//        redisTemplate.delete("dish_" + dishDTO.getCategoryId());
        clearCache("dish_" + dishDTO.getCategoryId());
        return Result.success();
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     *
     * 根据ID修改菜品启用禁用状态
     */
    @PostMapping("/status/{status}")
    @ApiOperation("根据ID修改菜品启用禁用状态")
    public Result<String> updateStatus(@PathVariable Integer status, Long id) {
        log.info("根据ID修改菜品启用禁用状态：{},{}", status, id);
        dishService.updateStatusById(status, id);

        // 清理所有的缓存数据
//        redisTemplate.delete(redisTemplate.keys("dish_*"));
        clearCache("dish_*");
        return Result.success();
    }

    /**
     * 根据ID查询菜品和对应的口味数据
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询菜品和对应的口味数据")
    public Result<DishDTO> getById(@PathVariable Long id) {
        log.info("根据ID查询菜品和对应的口味数据：{}", id);
        DishDTO dishDTO = dishService.getById(id);
        return Result.success(dishDTO);
    }

    /**
     * 修改菜品
     *
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateDish(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);

        // 清理所有的缓存数据
//        redisTemplate.delete(redisTemplate.keys("dish_*"));
        clearCache("dish_*");
        return Result.success();
    }

    /**
     * 根据ID批量删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据ID批量删除菜品")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("根据ID批量删除菜品：{}", ids);
        dishService.deleteBatch(ids);

        // 清理所有的缓存数据
//        redisTemplate.delete(redisTemplate.keys("dish_*"));
        clearCache("dish_*");

        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("查询菜品数据")
    public Result<List<Dish>> list(Dish dish) {
        log.info("查询菜品数据：{}", dish);
        List<Dish> list = dishService.list(dish);
        return Result.success(list);
    }

    /**
     * 清理缓存数据
     *
     * @param pattern
     */
    private void clearCache(String pattern) {
        Set<Object> keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
