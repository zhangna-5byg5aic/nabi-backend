package com.yupi.springbootinit.manager;

import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class AIManager {
    @Resource
    private YuCongMingClient yuCongMingClient;

    @Resource
    private AgentClient agentClient;

    @Resource
    private ZhiPuClient zhiPuClient;

    /**
     * 鱼聪明AI对话
     * @param message
     */
    public String doChat(Long modelId,String message)
    {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        System.out.println(response);
        return response.getData().getContent();
    }

    /**
     * 智谱清言AI对话
     * @param message
     */
    public String doAgentChat(String modelId,String message)
    {
        String result = agentClient.sendMessage(modelId,message);
        return result;
    }

    public String doQingChat(String goal,String data)
    {
        return zhiPuClient.testInvoke(goal,data);
    }
    public static List<String> extractTexts(String input) {
        String delimiter = "【【【【【";
        List<String> result = new ArrayList<>();
        String[] parts = input.split(delimiter);

        for (String part : parts) {
            String trimmedPart = part.trim();
            if (!trimmedPart.isEmpty()) {
                result.add(trimmedPart);
            }
        }
        // 使用正则表达式提取 JSON 代码
        Pattern pattern = Pattern.compile("(\\{.*\\})", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(result.get(0));

        if (matcher.find()) {
            String jsonCode = matcher.group(1);
            result.set(0,jsonCode);
//            System.out.println("jsonCode:"+jsonCode);
        } else {
            System.out.println("未找到 JSON 代码");
        }
        return result;
    }

}
