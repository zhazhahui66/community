package com.xxxx.controller;

import com.xxxx.entity.Comment;
import com.xxxx.entity.DiscussPost;
import com.xxxx.entity.Event;
import com.xxxx.event.EventProducer;
import com.xxxx.service.DiscussPostService;
import com.xxxx.service.impl.CommentService;
import com.xxxx.service.impl.DiscussPostServiceImpl;
import com.xxxx.util.CommunityConstant;
import com.xxxx.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostServiceImpl discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;


    @PostMapping("add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId")int discussPostId, Comment comment){
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        comment.setUserId(hostHolder.getUser().getId());
        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event()
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setUserId(comment.getUserId())
                .setTopic(TOPIC_COMMENT)
                .setData("postId",discussPostId);
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost target = discussPostService.findDiscussPostsById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if(comment.getEntityType() == ENTITY_TYPE_COMMENT){
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }

        eventProducer.fireEvent(event);
        return "redirect:/discuss/detail/"+ discussPostId;

    }
}
