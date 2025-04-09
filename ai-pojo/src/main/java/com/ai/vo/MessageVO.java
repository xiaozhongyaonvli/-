package com.ai.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 存储一轮聊天记录
 */
@Data
@Builder
public class MessageVO {

    /**角色 (user : 用户 assistant : ai助手)**/
    private String role;

    public static final String user = "user";
    public static final String assistant = "assistant";

    /**聊天消息**/
    private String content;
}
