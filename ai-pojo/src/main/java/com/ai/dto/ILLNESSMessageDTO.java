package com.ai.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接收前端返回的聊天消息
 */
@Data
public class ILLNESSMessageDTO {

    /**对话标题**/
    private String sessionName;

    /**聊天消息**/
    private String content;

    /**时间戳**/
    private LocalDateTime timeStamp;

    /**科室**/
    private String department;
}
