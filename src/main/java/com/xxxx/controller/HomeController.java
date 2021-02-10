package com.xxxx.controller;

import com.xxxx.entity.DiscussPost;
import com.xxxx.entity.Page;
import com.xxxx.service.DiscussPostService;
import com.xxxx.service.UserService;
import com.xxxx.service.impl.LikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @GetMapping("/index")
    public String getIndexPage(Model model,Page page){
        page.setPath("/index");
        page.setRows(discussPostService.findDiscussPostRows(0));
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list != null){
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post",post);
                map.put("user",userService.selectById(post.getUserId()));
                map.put("likeCount",likeService.findEntityLikeCount(post.getType(),post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);

        return "/index";
    }
    @GetMapping("/error")
    public String getErrorPage(){
        return "/error/500";
    }
}
