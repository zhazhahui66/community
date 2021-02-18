package com.xxxx.controller;

import com.xxxx.entity.Page;
import com.xxxx.service.ElasticsearchService;
import com.xxxx.service.UserService;
import com.xxxx.service.impl.LikeService;
import com.xxxx.service.impl.UserServiceImpl;
import com.xxxx.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping("/search")

    public String search(String keyword, Page page, Model model){
        List<Map<String, Object>> discussPostList = null;
        try {
            discussPostList = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Map<String, Object>> discussPostVOList = new ArrayList<>();
        if(discussPostList !=null){
            for (Map<String, Object> post : discussPostList) {
                HashMap<String, Object> map = new HashMap<>();
                //帖子
                map.put("post",post);
                //作者
                map.put("user",userService.selectById((Integer) post.get("userId"))  );
                map.put("likeCount",likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST, (Integer) post.get("id") ));
                discussPostVOList.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPostVOList);
        model.addAttribute("keyword",keyword);

        page.setPath("/search/"+keyword);
        page.setRows(discussPostList == null ?0 :discussPostList.size());

        return "/site/search";
    }
}
