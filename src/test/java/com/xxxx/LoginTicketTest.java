package com.xxxx;

import com.xxxx.entity.LoginTicket;
import com.xxxx.mapper.LoginTicketMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
@Slf4j
public class LoginTicketTest {
    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Test
    void testInsert(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setStatus(0);
        loginTicket.setTicket("abc");
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }
    @Test
    void test2(){
        LoginTicket ticket = loginTicketMapper.selectByTicket("abc");
        System.out.println(ticket);
        loginTicketMapper.updateStatus("abc",1);
        ticket = loginTicketMapper.selectByTicket("abc");
        System.out.println(ticket);

    }


}
