package com.xxxx.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AlphaServiceTest {

    @Autowired
    private AlphaService alphaService;
    @Test
    void save() {
        String res = alphaService.save();
        System.out.println(res);
    }
    @Test
    void save2() {
        String res = alphaService.save2();
        System.out.println(res);
    }
}