package com.ai.service;
import com.ai.dto.UserDTO;
import com.ai.result.Result;

import java.util.Map;

public interface UserService {
    /**
     * 检查电话是否已经注册过
     * @param phone  电话号码
     * @return       通过返回code200，已存在返回code409，资源冲突
     */
    Result chePhoneExists(String phone);

    /**
     * 用户注册
     * @param userDTO 用户提交信息
     * @return        TODO
     */
    Result register(UserDTO userDTO);

    /**
     * 发送验证码
     * @param phone 手机号码
     * @return      TODO
     */
    Result sendVerifyCode(String phone);

    /**
     * 用户登录
     * @param userDTO (手机号码+验证码  ||   手机号 + 密码)
     * @return        200成功返回token
     */
    Result login(UserDTO userDTO);
}
