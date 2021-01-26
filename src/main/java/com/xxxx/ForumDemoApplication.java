package com.xxxx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@MapperScan("com.xxxx.mapper")

public class ForumDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumDemoApplication.class, args);
    }

}
