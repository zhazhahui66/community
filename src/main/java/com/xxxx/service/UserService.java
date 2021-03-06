package com.xxxx.service;

import com.xxxx.entity.User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface UserService {
    User selectById(int id);
    User selectByName(String username);
    User selectByEmail(String email);
    int insertUser(User user);
    int updateStatus(int id,int status);
    int updateHeader(int id,String headerUrl);
    int updatePassword(int id,String password);

    Map<String,Object> register(User user);
    int activation(int userId,String code);
    Map<String, Object> login(String username,String password,int expiredSeconds);

}
