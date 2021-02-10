package com.xxxx;

import com.xxxx.entity.DiscussPost;
import com.xxxx.mapper.DiscussPostMapper;
import com.xxxx.util.MailClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@Slf4j
public class MailTest {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private TemplateEngine templateEngine;
    @Test
    public void testTextMail(){
        mailClient.sendMail("mhkl1314@163.com","nihc","fgadsg");
    }

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","kuangsheng");
        String content = templateEngine.process("/mail/demo", context);
        mailClient.sendMail("mhkl1314@163.com","HTML",content);

    }

    @Test
    void test3(){
        DiscussPost discussPost = discussPostMapper.selectDiscussPost(109);
        System.out.println(discussPost);
    }
}
