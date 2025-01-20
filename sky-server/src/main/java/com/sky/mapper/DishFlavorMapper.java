package com.sky.mapper;

import com.sky.entity.DishFlavor;

import java.util.List;

public interface DishFlavorMapper {
    /**
     * 批量插入口味列表数据
     * @param dishFlavorList
     */
    void insertBatch(List<DishFlavor> dishFlavorList);

    /**
     * 根据菜品id删除口味列表
     * @param dishIds
     */
    void deleteBatch(List<Long> dishIds);

    /**
     * 根据菜品id查询口味列表
     * @param dishId
     */
    List<DishFlavor> selectByDishId(Long dishId);

    /**
     * 根据菜品id删除口味列表
     * @param dishId
     */
    void deleteByDishId(Long dishId);
}
