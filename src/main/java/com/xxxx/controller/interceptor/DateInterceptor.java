package com.xxxx.controller.interceptor;

import com.xxxx.entity.User;
import com.xxxx.service.DateService;
import com.xxxx.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DateInterceptor implements HandlerInterceptor {
    @Autowired
    private DateService dateService;
    @Autowired
    private HostHolder hostHolder;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //统计UV
        String ip = request.getRemoteHost();
        dateService.recordUV(ip);

        //统计DAU
        User user = hostHolder.getUser();
        if(user != null){
            dateService.recordDAU(user.getId());
        }
        return true;
    }
}
