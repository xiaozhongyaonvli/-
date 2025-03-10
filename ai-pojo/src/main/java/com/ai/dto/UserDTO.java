package com.ai.dto;

import lombok.Data;

@Data
public class UserDTO {

    public String name;
    public String phone;
    public String pwd;
    public String identifyCode;  //验证码
}
