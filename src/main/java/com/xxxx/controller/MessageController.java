package com.xxxx.controller;

import com.alibaba.fastjson.JSONObject;
import com.xxxx.entity.Message;
import com.xxxx.entity.Page;
import com.xxxx.entity.User;
import com.xxxx.service.UserService;
import com.xxxx.service.impl.MessageService;
import com.xxxx.util.CommunityConstant;
import com.xxxx.util.CommunityUtil;
import com.xxxx.util.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
@Slf4j
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    //私信列表
    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page){

        User user = hostHolder.getUser();
        //分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        //会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation",message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(),message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target",userService.selectById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "site/letter";

    }

    @GetMapping("/letter/detail/{conversationId}")
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Page page,Model model){

        //分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId,page.getOffset(),page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter",message);
                map.put("fromUser",userService.selectById(message.getFromId()));
                letters.add(map);
            }
        }

        model.addAttribute("letters",letters);

        //私信目标
        model.addAttribute("target",getLetterTarget(conversationId));

        //改为已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()) {
            int i = messageService.readMessage(ids);
            log.info("更新了"+i+ "条私信伟已读");
        }

        return "/site/letter-detail";

    }

    @PostMapping("/letter/send/")
    @ResponseBody
    public String sendLetter(String toName,String content){
        User target = userService.selectByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1,"目标用户不存在!");
        }
        Message message = new Message();
        message.setContent(content);
        message.setToId(target.getId());
        message.setFromId(hostHolder.getUser().getId());
        if(message.getFromId() < message.getToId()){
            message.setConversationId(message.getFromId() +"_" + message.getToId());
        }   else {
            message.setConversationId(message.getToId() +"_" + message.getFromId());
        }
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }


    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.selectById(id1);
        } else {
            return userService.selectById(id0);
        }
    }

    //获取未读私信的id列表
    public List<Integer> getLetterIds(List<Message> letterList){
        List<Integer> ids = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                if(hostHolder.getUser().getId()==message.getToId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }
    @GetMapping("/notice/list")
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();

        //查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        if (message != null) {
            Map<String,Object> messageVO = new HashMap<>();
            messageVO.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user",userService.selectById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int noticeCount = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("noticeCount",noticeCount);
            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unreadCount",unreadCount);
            model.addAttribute("commentNotice",messageVO);
        }


        //查询评论类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        if (message != null) {
            Map<String,Object> messageVO = new HashMap<>();
            messageVO.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user",userService.selectById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int noticeCount = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("noticeCount",noticeCount);
            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unreadCount",unreadCount);
            model.addAttribute("likeNotice",messageVO);
        }

        //查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        if (message != null) {
            Map<String,Object> messageVO = new HashMap<>();
            messageVO.put("message",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user",userService.selectById((Integer) data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int noticeCount = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("noticeCount",noticeCount);
            int unreadCount = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unreadCount",unreadCount);
            model.addAttribute("followNotice",messageVO);
        }

        //查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        return "/site/notice";
    }

    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetail(@PathVariable("topic")String topic,Page page,Model model){
        User user = hostHolder.getUser();
        page.setLimit(5);
        //注意
        page.setPath(topic);
        page.setRows(messageService.findNoticeCount(user.getId(),topic));

        //获取通知列表
        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList =  new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String,Object> map = new HashMap<>();
                //通知
                map.put("notice",notice);
                //内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                HashMap data = JSONObject.parseObject(content, HashMap.class);
                map.put("user",userService.selectById((Integer) data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("entityId",data.get("entityId"));
                map.put("postId",data.get("postId"));
                //通知作者
                map.put("fromUser",userService.selectById(notice.getFromId()));
                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices",noticeVoList);

        //设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";
    }
}
