package com.xxxx;

import com.xxxx.entity.DiscussPost;
import com.xxxx.entity.User;
import com.xxxx.mapper.DiscussPostMapper;
import com.xxxx.service.DiscussPostService;
import com.xxxx.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
@Slf4j
class ForumDemoApplicationTests {
    @Autowired
    UserService userService;
    @Autowired
    DiscussPostService discussPostService;
    @Test
    void contextLoads() {
        User user = new User();
        user.setPassword("12346");
        user.setCreateTime(new Date());
        user.setSalt("abc");
        user.setHeaderUrl("afsajoflds");
        user.setEmail("123123@qq.com");
        user.setActivationCode("0");
        user.setStatus(0);
        user.setType(1);
        user.setUsername("123423");
        userService.insertUser(user);
    }
    @Test
    void test2(){
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(103, 0, 10);
        discussPosts.forEach(System.out::println);

        int i = discussPostService.findDiscussPostRows(103);
        System.out.println(i);
    }

    @Test
    void test3(){
        log.info("info..........");
        log.debug("debug..........");
        log.error("error............");
        log.warn("warn...............");
    }
}
