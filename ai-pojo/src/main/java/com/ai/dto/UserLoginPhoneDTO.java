package com.ai.dto;

import lombok.Data;

/**
 * 用户通过手机号和验证码
 */
@Data
public class UserLoginPhoneDTO {

    /**手机号码**/
    public String phone;

    /**验证码**/
    public String identifyCode; 
}
