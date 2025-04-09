package com.ai.utils;

import com.ai.properties.AliModelProperties;
import lombok.AllArgsConstructor;
import java.util.Arrays;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

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

}
