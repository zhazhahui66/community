package com.xxxx.event;

import com.alibaba.fastjson.JSONObject;
import com.xxxx.entity.DiscussPost;
import com.xxxx.entity.Event;
import com.xxxx.entity.Message;
import com.xxxx.service.DiscussPostService;
import com.xxxx.service.ElasticsearchService;
import com.xxxx.service.impl.DiscussPostServiceImpl;
import com.xxxx.service.impl.MessageService;
import com.xxxx.util.CommunityConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Component
@Slf4j
public class EventConsumer implements CommunityConstant {
    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostServiceImpl discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE})
    public void handleMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            log.error("消息的内容为空!");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            log.error("消息格式错误!");
            return;
        }

        //发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setCreateTime(new Date());
        message.setConversationId(event.getTopic());

        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        if(!event.getData().isEmpty()){
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    //发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            log.error("消息的内容为空!");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            log.error("消息格式错误!");
            return;
        }
        DiscussPost post = discussPostService.findDiscussPostsById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }
}
