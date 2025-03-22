package com.ai.dto;

import lombok.Data;

@Data
public class UserDTO {

    public String userName;
    public String phone;
    public String password;
    public String identifyCode;  //验证码
}