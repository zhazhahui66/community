package com.xxxx.controller;

import com.xxxx.entity.User;
import com.xxxx.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/select/{id}")
    public String selectById(@PathVariable int id){
        User user = userService.selectById(id);
        Assert.notNull(user,"该账号不存在!");
        return user.toString();
    }
}
