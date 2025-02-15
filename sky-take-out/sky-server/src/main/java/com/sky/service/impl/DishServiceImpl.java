package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.IDishService;
import com.sky.vo.DishVO;
import io.swagger.models.auth.In;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements IDishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;


    @Override
    public PageResult<DishVO> pageQuery(DishPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<DishVO> page = dishMapper.list(dto);
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void addDish(DishDTO dto) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dto, dish);
        // 保存菜品
        dishMapper.insert(dish);
        // 保存口味
        List<DishFlavor> flavors = dto.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(f -> {
                f.setDishId(dish.getId());
            });
            // 2.2 调用mapper保存方法,批量插入口味列表数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Transactional
    @Override
    public void delete(List<Long> ids) {
        // 1.删除菜品之前，需要判断菜品是否启售，启售中不允许删除
        ids.forEach(id -> {
            Dish dish = dishMapper.selectById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });
        // 2.需要判断菜品是否被套餐关联，关联了也不允许删除
        Integer count = setmealDishMapper.countByDishId(ids);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 3.删除菜品基本信息 dish表
        dishMapper.deleteBatch(ids);

        // 4.删除菜品口味列表信息 dish_flavor表
        dishFlavorMapper.deleteBatch(ids);
    }

    @Override
    public DishVO getById(Long id) {
        DishVO dishVO = new DishVO();

        // 1.根据菜品id查询菜品基本信息,封装到dishVO中
        Dish dish = dishMapper.selectById(id);
        BeanUtils.copyProperties(dish, dishVO);

        // 2.根据菜品id查询口味列表数据,封装到dishVO中
        List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(id);
        dishVO.setFlavors(flavors);

        // 3.返回DishVO对象
        return dishVO;
    }

    @Transactional
    @Override
    public void update(DishDTO dto) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dto, dish);
        // 1.修改菜品的基本信息，dish表
        dishMapper.update(dish);
        // 2.修改口味列表信息，dish_flavor表
        // 由于口味数据可能增加、可能删除、还可能修改口味的值，涉及到增删改操作，所以先全部删除旧数据，再添加新数据
        dishFlavorMapper.deleteByDishId(dto.getId());
        List<DishFlavor> flavors = dto.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            // 关联菜品id
            flavors.forEach(flavor -> {
                flavor.setDishId(dish.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Transactional
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setStatus(status);
        dish.setId(id);
        dishMapper.update(dish);
        // 如果是停售操作，还需要将包含当前菜品的套餐也停售
        if (status.equals(StatusConstant.DISABLE)) {
            // 联表查询setmeal、dish、setmeal_dish
            List<Setmeal> setmealList = setmealMapper.getByDishId(id);
            setmealList.forEach(setmeal -> {
                if (setmeal != null) {
                    setmeal.setStatus(StatusConstant.DISABLE);
                    setmealMapper.update(setmeal);
                }
            });
        }
    }

    @Override
    public List<Dish> getByCategoryId(Long categoryId, String name, Integer status) {
        return dishMapper.getByCategoryId(categoryId, name, status);
    }

    /**
     * 条件查询菜品和口味
     *
     * @param dish
     * @return
     */
    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.getByCategoryId(dish.getCategoryId(), null, dish.getStatus());
        ArrayList<DishVO> dishVOS = new ArrayList<>();
        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);
            dishVO.setFlavors(dishFlavorMapper.selectByDishId(d.getId()));
            dishVOS.add(dishVO);
        }
        return dishVOS;
    }
}
