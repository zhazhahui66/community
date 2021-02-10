package com.xxxx.util;

import com.xxxx.entity.User;
import org.springframework.stereotype.Component;

/*
 * 持有用户信息,代替Session对象
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal();
    public void setUsers(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }
    public void clear(){
        users.remove();
    }

}
