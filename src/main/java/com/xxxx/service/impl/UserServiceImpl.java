package com.xxxx.service.impl;

import com.xxxx.entity.User;
import com.xxxx.mapper.UserMapper;
import com.xxxx.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    public UserMapper userMapper;
    @Override
    public User selectById(int id) {
        return userMapper.selectById(id);
    }

    @Override
    public User selectByName(String username) {
        return userMapper.selectByName(username);
    }

    @Override
    public User selectByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public int insertUser(User user) {
        return userMapper.insertUser(user);
    }

    @Override
    public int updateStatus(int id, int status) {
        return userMapper.updateStatus(id,status);
    }

    @Override
    public int updateHeader(int id, String headerUrl) {
        return userMapper.updateHeader(id,headerUrl);
    }

    @Override
    public int updatePassword(int id, String password) {
        return userMapper.updatePassword(id,password);
    }
}
