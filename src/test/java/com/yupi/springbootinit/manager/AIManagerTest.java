package com.yupi.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AIManagerTest {
    @Resource
    private AIManager aiManager;

    @Test
    void doChat() {
        String answer= aiManager.doChat(1793560830003585026L,new String("日期,用户数\n" +
                "1,10\n" +
                "2,20\n" +
                "3,30\n"));
        System.out.println("answer: " + answer);
    }
}