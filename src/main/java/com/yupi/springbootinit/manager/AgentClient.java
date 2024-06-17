package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.constant.CommonConstant;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Service
public class AgentClient {
    private static String accessToken;
    public AgentClient()
    {
        accessToken = getAccessToken(CommonConstant.QING_API_KEY,CommonConstant.QING_API_SECRET);
    }
    public static String handleResponse(JSONObject dataDict) {
        String message = dataDict.optString("message", null);
        if (message != null && !message.isEmpty()) {
            JSONObject content = null;
            try {
                content = new JSONObject(message).optJSONObject("content");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            if (content != null && content.length() > 0) {
                String responseType = content.optString("type");
                switch (responseType) {
                    case "text":
                        return content.optString("text", "No text provided");
                    case "image":
                        JSONArray images = content.optJSONArray("image");
                        StringBuilder imageUrls = new StringBuilder();
                        for (int i = 0; i < images.length(); i++) {
                            if (i > 0) imageUrls.append(", ");
                            try {
                                imageUrls.append(images.getJSONObject(i).optString("image_url"));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return imageUrls.toString();
                    case "code":
                        return content.optString("code");
                    case "execution_output":
                    case "system_error":
                        return content.optString("content");
                    case "tool_calls":
                        return dataDict.optString("tool_calls");
                    case "browser_result":
                        JSONObject browserContent = null;
                        try {
                            browserContent = new JSONObject(content.optString("content", "{}"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        return "Browser Result - Title: " + browserContent.optString("title") + " URL: " + browserContent.optString("url");
                    default:
                        return "Unknown response type";
                }
            }
        }
        return "No message content";
    }

    public  String sendMessage(String assistantId, String prompt) {
        String urlString = "https://chatglm.cn/chatglm/assistant-api/v1/stream";
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000); // Set connection timeout
            connection.setReadTimeout(5000); // Set read timeout

            JSONObject data = new JSONObject();
            data.put("assistant_id", assistantId);
            data.put("prompt", prompt);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = data.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int status = connection.getResponseCode();
            if (status == 200) {
                try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())) {
                    StringBuilder responseStrBuilder = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        responseStrBuilder.append(scanner.nextLine());
                    }
                    String responseBody = responseStrBuilder.toString();
//                    System.out.println(responseBody);
//                    return responseBody;
                    for (String line : responseBody.split("\n")) {
                        if (line.startsWith("message:")) {
                            JSONObject dataDict = new JSONObject(line.substring(5));
                            String output = handleResponse(dataDict);
                            System.out.println(output);
                            return output;
                        }
                    }
                }
            } else {
                System.out.println("Request failed: " + status);
                accessToken = getAccessToken(CommonConstant.QING_API_KEY,CommonConstant.QING_API_SECRET);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 智谱清言API获取AccessToken
     * @param apiKey
     * @param apiSecret
     * @return
     */
    public static String getAccessToken(String apiKey, String apiSecret) {
        String urlString = "https://chatglm.cn/chatglm/assistant-api/v1/get_token";
        String token = "";

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            JSONObject jsonInput = new JSONObject();
            jsonInput.put("api_key", apiKey);
            jsonInput.put("api_secret", apiSecret);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInput.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8.name())) {
                String response = scanner.useDelimiter("\\A").next();
                JSONObject jsonResponse = new JSONObject(response);
                token = jsonResponse.getJSONObject("result").getString("access_token");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return token;
    }
}
