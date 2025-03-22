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

    /**用户id**/
    private Integer id;

    /**用户名(不允许重复，考虑到老人可能实名，而姓名重复)**/
    private String  userName;

    /**电话号码(唯一)**/
    private String  phone;

    /**本地头像(图片地址)(传递给前端通过阿里云oss)**/
    private String  avatarUrl;

    /**用户密码**/
    private String  password;
}
