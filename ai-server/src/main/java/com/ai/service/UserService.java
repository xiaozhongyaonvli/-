package com.ai.service;
import com.ai.dto.UserDTO;
import com.ai.dto.UserLoginPhoneDTO;
import com.ai.dto.UserLoginPwdDTO;
import com.ai.result.Result;
import com.ai.vo.UserLoginVO;

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
    Result<UserLoginVO> register(UserDTO userDTO);

    /**
     * 发送验证码
     * @param phone 手机号码
     * @return      TODO
     */
    Result sendVerifyCode(String phone);

    /**
     * 根据电话号码+验证码登录
     * @param userLoginPhoneDTO （手机号+验证码）
     * @return   返回登录与否信息
     */
    Result<UserLoginVO> loginWithPhone(UserLoginPhoneDTO userLoginPhoneDTO);

    /**
     * 用户登录(用户名+密码)
     * @param userLoginPwdDTO (用户名+密码)
     * @return                200成功返回token
     */
    Result<UserLoginVO> loginWithPwd(UserLoginPwdDTO userLoginPwdDTO);
}
