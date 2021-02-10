package com.xxxx.service.impl;

import com.xxxx.entity.DiscussPost;
import com.xxxx.entity.User;
import com.xxxx.mapper.DiscussPostMapper;
import com.xxxx.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
@Service
public class AlphaService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public String save(){
        User user = new User();
        user.setCreateTime(new Date());
        user.setUsername("11111111564");
        user.setPassword("21e3hrfwdskj");
        user.setEmail("213erwfd@qq.com");
        DiscussPost post = new DiscussPost();
        post.setCreateTime(new Date());
        post.setTitle("你好我是事务管理");
        post.setContent("这里是内容内容内容!!!!!!!!!!!!!!!!!!!!!!!!");
        userMapper.insertUser(user);
        discussPostMapper.publishDiscussPost(post);

        Integer n = Integer.valueOf("abc");
        return "ok";
    }

    public String save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<String>() {
            @Override
            public String doInTransaction(TransactionStatus transactionStatus) {
                User user = new User();
                user.setCreateTime(new Date());
                user.setUsername("11111111564");
                user.setPassword("21e3hrfwdskj");
                user.setEmail("213erwfd@qq.com");
                DiscussPost post = new DiscussPost();
                post.setCreateTime(new Date());
                post.setTitle("你好我是事务管理");
                post.setContent("这里是内容内容内容!!!!!!!!!!!!!!!!!!!!!!!!");
                userMapper.insertUser(user);
                int i = 1/0;

                discussPostMapper.publishDiscussPost(post);


                return "ok";
            }
        });
    }
}
