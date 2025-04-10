package com.ai.utils;

import cn.hutool.json.JSONUtil;
import com.ai.properties.AliModelProperties;
import com.ai.result.Result;
import com.alibaba.dashscope.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;

import static javax.swing.text.html.parser.DTDConstants.MODEL;

@AllArgsConstructor
public class AliModelUtil {

    private AliModelProperties aliModelProperties;

    /**
     * 工具方法，输入ai的提示词和user的信息 返回ai回答
     * @param  sysContent             ai的提示content
     * @param  userContent            用户传入的描述
     * @throws NoApiKeyException      apikey失效
     * @throws InputRequiredException 未提供输入异常
     * @return 返回ai回答
     */
    public String chatAi(String sysContent,String userContent) throws NoApiKeyException, InputRequiredException {
        Generation gen = new Generation(); // 创建一个 Generation 实例用于生成请求

        // 创建系统消息，指示 AI 的角色和任务
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(sysContent)
                .build();

        // 创建用户消息，包含用户的描述
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(userContent) // 使用传入的描述
                .build();

        // 构建请求参数，包含 API 密钥、模型名称、消息列表等
        GenerationParam param = GenerationParam.builder()
                .apiKey(aliModelProperties.getApiKey()) // 从配置中获取API密钥
                .model("deepseek-r1") // 使用的模型名称
                .messages(Arrays.asList(systemMsg, userMsg)) // 传入的消息列表，包括系统和用户的消息
                .resultFormat(GenerationParam.ResultFormat.MESSAGE) // 设置返回结果的格式为消息
                .build();
        // 调用 Generation 的接口，获取结果
        GenerationResult result = gen.call(param);
        // 返回结果中的第一条选择的内容
        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }



    //流式编程方法
    public static Result<StreamingResponseBody> streamData(@NotNull String content,HttpServletResponse response) {


        Map<String, String> map = new HashMap<>();
        map.put("content","空");
        //将content的内容整除后向上取整
        double len = Math.ceil(content.length()*1.0/10);
        StreamingResponseBody responseBody = outputStream -> {
            try (PrintWriter writer = new PrintWriter(outputStream)) {
                String content1 = content;
                for (int i = 1; i <= len; i++) {

                    if(content1.length()>=10){
                        map.put("content", content.substring(0,10));
                    }else{
                        map.put("content", content);
                    }

//                    writer.println(JsonUtils.to);
//                    writer.flush();
                    // 模拟一些延迟
                    writer.print(JSONUtil.toJsonStr(map));
                    writer.flush();
                    if(content1.length()>10){
                        content1 = content1.substring(10);
                    }else{
                        break;
                    }
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.TEXT_PLAIN);

//        return Result.ok()
//                .headers(headers)
//                .body(responseBody);

        response.setHeader("Content-Type", "text/plain");
        return Result.success(responseBody);
    }


}
