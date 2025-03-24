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

    public String getDiagnosis(String illnessDesc) throws NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();

        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("你是一位经验丰富的医生，请根据患者的描述进行科室分类建议。你只能回答'皮肤科'或'肝脏科'，不要提供任何其他解释或描述。")
                .build();

        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(illnessDesc)
                .build();

        GenerationParam param = GenerationParam.builder()
                .apiKey(aliModelProperties.getApiKey())
                .model("deepseek-r1")
                .messages(Arrays.asList(systemMsg,userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

        GenerationResult result = gen.call(param);
        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }
}
