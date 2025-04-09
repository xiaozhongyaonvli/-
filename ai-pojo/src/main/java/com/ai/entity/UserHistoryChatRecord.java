package com.ai.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 记录用户的历史记录(记录于数据库)
 * userId + sessionName 可以找到唯一的对话(联合索引)
 * 查看OSS对应地址再通过地址拿到json文件
 */
@Data
public class UserHistoryChatRecord {

    /**id主键**/
    private Integer id;

    /**用户id**/
    private Integer userId;

    /**对话标题**/
    private String sessionName;

    /**对话json对应的oss地址**/
    private String ossTextJsonUrl;

    /**患者上传的图片对应的oss地址**/
    private String ossImageUrl;

    /**时间戳**/
    private LocalDateTime timeStamp;
}