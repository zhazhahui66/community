package com.xxxx.controller;


import com.xxxx.entity.*;
import com.xxxx.event.EventProducer;
import com.xxxx.service.DiscussPostService;
import com.xxxx.service.UserService;
import com.xxxx.service.impl.CommentService;
import com.xxxx.service.impl.LikeService;
import com.xxxx.service.impl.UserServiceImpl;
import com.xxxx.util.CommunityConstant;
import com.xxxx.util.CommunityUtil;
import com.xxxx.util.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@Slf4j
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private CommentService commentService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private LikeService likeService;
    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title,String context){
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403,"您还没有登录哦!");
        }
        DiscussPost post = new DiscussPost();
        post.setContent(context);
        post.setTitle(title);
       /* post.setCommentCount(0);
        post.setScore(0);
        post.setStatus(0);
        post.setType(0);*/
        post.setCreateTime(new Date());
        post.setUserId(user.getId());
        discussPostService.addDiscussPost(post);

        //触发发帖事件
        Event event = new Event().setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityId(post.getId())
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0,"发布成功!");
    }

    @GetMapping("/detail/{discussPostId}")
    public String detail(@PathVariable("discussPostId") String discussPostId, Model model, Page page){

        //帖子
        DiscussPost post = discussPostService.findDiscussPostsById(Integer.parseInt(discussPostId));
        model.addAttribute("post",post);
        //作者
        User user = userService.selectById(post.getUserId());
        model.addAttribute("user",user);

        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
        //点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0:
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_POST, post.getId());
        model.addAttribute("likeCount",likeCount);
        model.addAttribute("likeStatus",likeStatus);

        //分页设置
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

       /* List<Comment> commentsList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());

        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentsList != null) {
            for (Comment comment : commentsList) {
                Map<String, Object> commentVo = new HashMap<>();
                //评论
                commentVo.put("comment",comment);
                //评论者
                commentVo.put("user",userService.selectById(comment.getUserId()));
                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复OV列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        //回复
                        replyVo.put("reply", reply);
                        //回复者
                        replyVo.put("user", userService.selectById(reply.getUserId()));
                        //回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.selectById(reply.getTargetId());
                        replyVo.put("target", target);
                        replyVoList.add(replyVo);
                    }
                }//回复列表
                commentVo.put("replys",replyList);

                //回复数量
                int replyCount = commentService.findCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount",replyCount);

                    //
                commentVoList.add(commentVo);

            }
        }

        model.addAttribute("comments",commentVoList);*/

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", userService.selectById(comment.getUserId()));

                //点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                //点赞状态
                likeStatus = hostHolder.getUser() == null ? 0:
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);
                commentVo.put("likeStatus",likeStatus);



                // 回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.selectById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.selectById(reply.getTargetId());
                        replyVo.put("target", target);

                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        //点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0:
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);
                        replyVo.put("likeStatus",likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.findCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }

    //置顶
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id){
        discussPostService.updateType(id,1);
        return CommunityUtil.getJSONString(0);
    }

    //加精
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int id){
        discussPostService.updateStatus(id,1);
        return CommunityUtil.getJSONString(0);
    }

    //删除
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id){
        discussPostService.updateStatus(id,2);
        return CommunityUtil.getJSONString(0);
    }
}
