package com.ai.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 存储用户一个对话的多轮聊天记录
 * 缓存于redis
 * 并转换为json文件存于阿里云oss
 * 或者从阿里云oss取下json转为该对象用于其他业务
 */
@Data
public class UserChatRecordVO {

    /**患者图片OSS地址**/
    private String imageOSS;

    /**对话列表**/
    private List<MessageVO> messageVOList;

    /**时间戳**/
    private LocalDateTime timeStamp;
}
