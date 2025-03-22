package com.ai.dto;

import lombok.Data;

/**
 * 用户登录(用户名+手机号码)
 */
@Data
public class UserLoginPwdDTO {

    /**用户名**/
    public String userName;

    /**用户密码**/
    private String  password;
}
