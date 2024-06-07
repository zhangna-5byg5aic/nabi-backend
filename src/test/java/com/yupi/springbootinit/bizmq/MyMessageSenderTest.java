package com.yupi.springbootinit.bizmq;

import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class MyMessageSenderTest {

    @Resource
    private MyMessageSender myMessageSender;
    @Test
    void sendMessage() {
        myMessageSender.sendMessage("code_exchange","my_routingKey","你好你好");
    }
}