package com.ai.constant;

public class AliOSSConstant {

    // 存放头像的前缀目录 （image/+UUID+userId+extern即后缀名(.jpg .png)）
    public static final String IMAGE_URL_PREFIX = "image/";

    // 存放患者上传图片或者X光 （patient/image/+UUID+userId+sessionName）
    public static final String PATIENT_IMAGE = "patient/image/";

    // 存放历史聊天记录json文件地址 （chat/history/+UUID+userId+sessionName）
    public static final String CHAT = "chat/history/";
}