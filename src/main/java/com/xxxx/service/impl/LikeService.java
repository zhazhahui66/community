package com.xxxx.service.impl;

import com.xxxx.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    //点赞
    public void like(int userId,int entityType,int entityId,int entityUserId){
      /*  //实体key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if(isMember){
            redisTemplate.opsForSet().remove(entityLikeKey,userId);
        } else {
            redisTemplate.opsForSet().add(entityLikeKey,userId);
        }*/
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {

                //获取key
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
                redisOperations.multi();
                if(isMember){
                    redisOperations.opsForSet().remove(entityLikeKey,userId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                } else {
                    redisOperations.opsForSet().add(entityLikeKey,userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }
                return redisOperations.exec();
            }
        });
    }

    //查询某实体点赞的数量
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //点赞的返回1  未点点赞0+
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId) ? 1 : 0;
    }

    //查询某个用户获得的赞
    public int findUserLikeCount(int userId){
        String entityLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer likeCount =(Integer) redisTemplate.opsForValue().get(entityLikeKey);
        return  likeCount == null ? 0 : likeCount.intValue();
    }
}
