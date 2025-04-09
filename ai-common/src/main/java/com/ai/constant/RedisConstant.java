package com.ai.constant;

public class RedisConstant {

    // 用于存储手机验证码
    public static final String PhoneCode = "phone:code:";
    // 用于存储用户的历史聊天记录标题列表 user:record:+userId
    public static final String Record    = "user:record:";
    // 用于记录指定用户的指定聊天详细记录(com.ai.vo.UserChatRecordVO) user:chat:+userId+sessionName
    public static final String userIdSessionName = "user:chat:"; // key
    public static final String messageVOList = "messageVOList";  // hashKey
}
