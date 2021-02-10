package com.xxxx.controller;

import com.xxxx.entity.Event;
import com.xxxx.entity.Page;
import com.xxxx.entity.User;
import com.xxxx.event.EventProducer;
import com.xxxx.service.UserService;
import com.xxxx.service.impl.FollowService;
import com.xxxx.util.CommunityConstant;
import com.xxxx.util.CommunityUtil;
import com.xxxx.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;
    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.follow(user.getId(),entityType,entityId);

        //触发关注事件
        Event event = new Event()
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setUserId(user.getId())
                .setEntityUserId(entityId)
                .setTopic(TOPIC_FOLLOW);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0,"已关注!");
    }

    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(),entityType,entityId);

        return CommunityUtil.getJSONString(0,"已取消关注!");
    }

    //用户粉丝列表
    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId")int userId, Page page, Model model){
        User user = userService.selectById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/follower/"+userId);
        page.setRows((int) followService.findFollowerCount(3,userId));
        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if(userList != null){
            for (Map<String, Object> map : userList) {
                User u = (User)map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/follower";
    }


    //用户关注列表
    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId")int userId, Page page, Model model){
        User user = userService.selectById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followee/"+userId);
        page.setRows((int) followService.findFolloweeCount(userId,3));
        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if(userList != null){
            for (Map<String, Object> map : userList) {
                User u = (User)map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/followee";
    }
    //查询用户是否有关注该实体
    private boolean hasFollowed(int userId){
        if(hostHolder.getUser() == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(),3,userId);
    }
}
