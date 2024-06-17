package com.yupi.springbootinit.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.zhipu.oapi.service.v4.api.ChatApiService.defaultObjectMapper;

@Service
public class ZhiPuClient {
    ClientV4 client = new ClientV4.Builder("87b0060a99af08b3e95145ef0dff335c.jIvIJ11RUH8urfYj").build();
    private  final ObjectMapper mapper = defaultObjectMapper();

    public String testInvoke(String goal,String data) {


        String messagePram = prompt_template(goal,data);
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), messagePram);
        messages.add(chatMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);

        // 创建 ObjectMapper 实例
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonData = mapper.writeValueAsString(invokeModelApiResp);
            // 解析 JSON 数据
            JsonNode rootNode = objectMapper.readTree(jsonData);

            // 提取消息内容
            String messageContent = rootNode
                    .path("data")
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            return messageContent;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String prompt_template(String goal,String data)
    {
        String template = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容:\n" +
                "## 分析目标:\n" +
                "{0}\n" +
                "## 原始数据:\n" +
                "{1}\n"+
                "请根据这两部分内容，按照以下指定格式生成内容(此外不要输出任何多余的开头、结尾、注释)：\n" +
                "【【【【【\n" +
                "(前端 Echarts v5 的 option 配置对象js代码，合理地将数据进行可视化，确保代码正确，代码JSON格式正确，不要生成任何多余的开头、结尾、注释)\n" +
                "【【【【【\n" +
                "(明确的数据分析结论、越详细越好，不要生成多余的注释)";
        return MessageFormat.format(template, goal, data);
    }


}
