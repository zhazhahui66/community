package com.xxxx;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    void test(){
        redisTemplate.opsForValue().set("key1","value1");
        System.out.println(redisTemplate.opsForValue().get("key1"));
    }

    @Test
    void test2(){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                redisTemplate.multi();
                redisTemplate.opsForSet().add("key:set","zhangshang");
                redisTemplate.opsForSet().add("key:set","lisi");
                redisTemplate.opsForSet().add("key:set","wangwu");
                System.out.println( redisTemplate.opsForSet().members("key:set"));
                return redisTemplate.exec();
            }
        });
        System.out.println( redisTemplate.opsForSet().members("key:set"));
    }
}
