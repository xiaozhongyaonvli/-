package com.ai.vo;

import lombok.Data;

@Data
public class UserLoginVO {

    /**用户名称**/
    private String userName;

    /**头像地址(图片地址)(传递给前端通过阿里云oss)**/
    private String  avatarUrl;

    /**Token**/
    private String token;
}
