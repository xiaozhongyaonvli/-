package com.ai.service;
import com.ai.dto.ILLNESSMessageDTO;
import com.ai.dto.UserDTO;
import com.ai.dto.UserLoginPhoneDTO;
import com.ai.dto.UserLoginPwdDTO;
import com.ai.result.Result;
import com.ai.vo.MessageVO;
import com.ai.vo.UserLoginVO;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
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

    /**
     * 与ai进行聊天
     * @param illnessMessageDTO 用户消息
     * @return                  返回ai响应信息
     */
    Result<StreamingResponseBody> chat(ILLNESSMessageDTO illnessMessageDTO, HttpServletResponse response) throws NoApiKeyException, InputRequiredException;

    /**
     * 获取用户历史聊天标题列表
     * @param userId 用户id
     * @return       返回聊天标题列表
     */
    List<String> historyRecordTitleList(Integer userId);

    /**
     * 更新用户头像
     * @param file 用户上传头像图片文件
     * @return     返回OSS地址
     */
    String updateAvatar(MultipartFile file) throws IOException;

    /**
     * 根据用户上传患处图片或者X光获取细分科室
     * @param image 患处图片或者X光
     * @param desc  用户初步描述
     * @return      返回对话标题
     */
    String aiDepartmentSortByImage(MultipartFile image, String desc);
}
