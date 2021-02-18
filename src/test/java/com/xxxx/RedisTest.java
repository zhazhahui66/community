package com.xxxx;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.io.IOException;

@SpringBootTest
public class RedisTest {

    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

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

    @Test
    void testExistIndex()throws IOException {
        GetIndexRequest request = new GetIndexRequest("index_1");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }
}
