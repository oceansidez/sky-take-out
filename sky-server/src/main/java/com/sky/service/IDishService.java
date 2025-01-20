package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface IDishService {
    PageResult<DishVO> pageQuery(DishPageQueryDTO dto);

    void addDish(DishDTO dto);

    void delete(List<Long> ids);

    DishVO getById(Long id);

    void update(DishDTO dto);

    void startOrStop(Integer status, Long id);

    List<Dish> getByCategoryId(Long categoryId, String name);
}
