package com.yupi.springbootinit.manager;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.zhipu.oapi.service.v4.api.ChatApiService.defaultObjectMapper;
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
    @Test
    void doQingChat(){
        String modelId = "666c12d90c262ddb16102a44";
        String prompt = "分析目标：分析网站用户增长情况，饼图\n" +
                "原始数据：日期, 用户数\n" +
                "1, 10\n" +
                "2, 20\n" +
                "3, 30\n" +
                "4, 80\n" +
                "5,20\n" +
                "6, 60\n" +
                "7, 0\n" +
                "8, 10\n" +
                "9, 20";
        aiManager.doAgentChat(modelId,prompt);
    }
    @Test
    void testDoQingChat() {
        String goal="分析网站用户增长情况，饼图";
        String data="日期, 用户数\n" +
                "1, 10\n" +
                "2, 20\n" +
                "3, 30\n" +
                "4, 80\n" +
                "5,20\n" +
                "6, 60\n" +
                "7, 0\n" +
                "8, 10\n" +
                "9, 20";
        List<String> res=AIManager.extractTexts(aiManager.doQingChat(goal,data));
        System.out.println(res);

    }
}