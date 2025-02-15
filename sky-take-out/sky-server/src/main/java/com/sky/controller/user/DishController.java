package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.IDishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@Api(tags = "菜品相关接口")
@RequestMapping("/user/dish")
@RestController("userDishController")
public class DishController {

    @Autowired
    private IDishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    @ApiOperation("根据分类id查询菜品")
    @GetMapping("/list")
    public Result<List<DishVO>> list(Long categoryId, String name) {
        log.info("根据分类id查询菜品: categoryId={}, name={}", categoryId, name);
        //构造redis中的key，规则：dish_分类id
        String key = "dish_" + categoryId;
        List<DishVO> dishVOS = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if (dishVOS != null && dishVOS.size() > 0) {
            return Result.success(dishVOS);
        }
        // 如果没有缓存数据，查询MySQL数据库
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品
        dishVOS = dishService.listWithFlavor(dish);
        //如果不存在，查询数据库，将查询到的数据放入redis中
        redisTemplate.opsForValue().set(key, dishVOS);
        return Result.success(dishVOS);
    }
}
