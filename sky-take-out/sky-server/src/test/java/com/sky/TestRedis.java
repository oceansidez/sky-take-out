package com.sky;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//如果用了webSocket，测试环境需要声明webEnvironment，否则报错
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestRedis {
    @Autowired
    private RedisTemplate redisTemplate;

    // 测试String类型操作
    @Test
    public void testString() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("name", "zhangsan");
        Object name = valueOperations.get("name");
        System.out.println("name = " + name);
        // 需求：存储一个验证码，并且设置过期时间为10s
        valueOperations.set("code", "1234", 2, TimeUnit.MINUTES);

        Object code = valueOperations.get("code");
        System.out.println("code = " + code);

        Boolean lock = valueOperations.setIfAbsent("lock", "1");
        Boolean lock1 = valueOperations.setIfAbsent("lock", "1");
        System.out.println("lock = " + lock);
        System.out.println("lock1 = " + lock1);
    }

    @Test
    public void testHash() {
        HashOperations hashOperations = redisTemplate.opsForHash();
        // 2.通过HashOperations对象，操作hash类型数据
        hashOperations.put("user", "name", "zhangsan");
        hashOperations.put("user", "age", 18);
        hashOperations.put("user", "gender", "男");

        Object name = hashOperations.get("user", "name");
        System.out.println("name = " + name);

        Set user = hashOperations.keys("user");
        System.out.println("user = " + user);

        List user1 = hashOperations.values("user");
        System.out.println("user1 = " + user1);

        Map user2 = hashOperations.entries("user");
        System.out.println("user2 = " + user2);
    }


    @Test
    public void testSet() {
        SetOperations setOperations = redisTemplate.opsForSet();

        // 2.通过SetOperations对象，操作Set类型数据
        setOperations.add("set01", "java", "go", "python", "C");
        setOperations.add("set02", "java", "go", "sing", "dance");
        Set set01 = setOperations.members("set01");
        Set set02 = setOperations.members("set02");
        System.out.println("set01 = " + set01);
        System.out.println("set02 = " + set02);

        // 求交集
        Set intersect = setOperations.intersect("set01", "set02");
        System.out.println("交集：" + intersect);

        // 求并集
        Set union = setOperations.union("set01", "set02");
        System.out.println("并集：" + union);

        // 求差集，set01 - set02
        Set difference = setOperations.difference("set01", "set02");
        System.out.println("差集：" + difference);
    }

    // 测试ZSet类型操作
    @Test
    public void testZSet() {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();

        // 2.通过zSetOperations对象，操作ZSet类型数据
        zSetOperations.add("zset01", "zhangsan", 88);
        zSetOperations.add("zset01", "lisi", 79);
        zSetOperations.add("zset01", "wangwu", 99.9);

        // 获取所有
        Set zset01 = zSetOperations.range("zset01", 0, -1);
        System.out.println("zset01 = " + zset01);

        Set<DefaultTypedTuple> zset011 = zSetOperations.rangeWithScores("zset01", 0, -1);
        System.out.println("zset011 = " + zset011);
        for (DefaultTypedTuple tuple : zset011) {
            System.out.println(tuple.getValue() + "--" + tuple.getScore());
        }
    }

    // 测试List类型操作
    @Test
    public void testList() {
        ListOperations listOperations = redisTemplate.opsForList();
        // 2.通过ListOperations对象，操作List类型数据
        listOperations.leftPushAll("list01", "aaa", "bbb", "ccc");
        listOperations.rightPushAll("list01", "111", "222", "333");

        List list01 = listOperations.range("list01", 0, -1);
        System.out.println("list01 = " + list01);

        System.out.println("=====================================");
        Object object = listOperations.rightPop("list01");
        System.out.println("删除的元素 = " + object);
        list01 = listOperations.range("list01", 0, -1);
        System.out.println("list01 = " + list01);
    }

    /**
     * 通用命令操作
     */
    @Test
    public void testCommon() {
        // 获取所有的key
        Set keys = redisTemplate.keys("*");
        System.out.println(keys);

        Boolean user1 = redisTemplate.hasKey("list01");
        System.out.println("user1 = " + user1);
        for (Object key : keys) {
            DataType type = redisTemplate.type(key);
            System.out.println("type = " + type);
        }

        // 删除所有的key
        redisTemplate.delete(keys);
    }

}
