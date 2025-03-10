package com.ai.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private Integer id;          //用户id
    private String  name;        //用户名(允许重复，考虑到老人可能实名，而姓名重复)
    private String  phone;       //电话号码(唯一)
    private String  avatarUrl;  //本地头像(图片地址)(传递给前端通过阿里云oss)
}
