package com.ai.controller;

import com.ai.constant.MsgConstant;
import com.ai.context.BaseContext;
import com.ai.dto.ILLNESSMessageDTO;
import com.ai.dto.UserDTO;
import com.ai.dto.UserLoginPhoneDTO;
import com.ai.dto.UserLoginPwdDTO;
import com.ai.properties.ImageProperties;
import com.ai.result.Result;
import com.ai.service.UserService;
import com.ai.utils.AliOSSUtil;
import com.ai.vo.UserLoginVO;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@Slf4j
@Api(tags = "用户相关接口")
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:5173")  // 允许前端访问
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private ImageProperties imageProperties;
    @Autowired
    private AliOSSUtil aliOSSUtil;

    /**
     * 注册时，检验电话号码是否已经注册过
     * @param phone  电话号码
     * @return       通过返回code200，已存在返回code409，资源冲突
     */
    @GetMapping("/register/{phone}")
    @ApiOperation("注册手机查重接口")
    public Result checkPhoneExists(@PathVariable String phone){
        boolean flag = checkPhone(phone);
        if (!flag) return Result.error(400, MsgConstant.phoneFormatError);
        return userService.chePhoneExists(phone);
    }

    /**
     * 用户注册
     * @param userDTO 用户提交信息
     * @return        400手机号格式错误
     */
    @PostMapping("/register")
    @ApiOperation("注册接口")
    public Result<UserLoginVO> register(@RequestBody UserDTO userDTO){
        boolean flag = checkPhone(userDTO.getPhone());
        if (!flag) return Result.error(400, MsgConstant.phoneFormatError);
        return userService.register(userDTO);
    }

    /**
     * 发送验证码
     * @param phone 手机号码0000
     * @return      400手机号格式错误
     */
    @GetMapping("/code")
    @ApiOperation("发送验证码接口")
    public Result sendVerifyCode(@RequestParam String phone){
        boolean flag = checkPhone(phone);
        if (!flag) return Result.error(400, MsgConstant.phoneFormatError);
        return userService.sendVerifyCode(phone);
    }

    /**
     * 用户登录(手机号+验证码)
     * @param userLoginPhoneDTO (手机号码+验证码  ||   手机号 + 密码)
     * @return                  200成功返回token
     */
    @PostMapping("/login/phone")
    @ApiOperation("用户手机登录接口")
    public Result<UserLoginVO> loginWithPhone(@RequestBody UserLoginPhoneDTO userLoginPhoneDTO){
        boolean flag = checkPhone(userLoginPhoneDTO.getPhone());
        if (!flag) return Result.error(400, MsgConstant.phoneFormatError);
        return userService.loginWithPhone(userLoginPhoneDTO);
    }

    /**
     * 用户登录(用户名+密码)
     * @param userLoginPwdDTO (用户名+密码)
     * @return                200成功返回token
     */
    @PostMapping("/login/name")
    @ApiOperation("用户名称密码登录接口")
    public Result<UserLoginVO> loginWithNameAndPwd(@RequestBody UserLoginPwdDTO userLoginPwdDTO){
        return userService.loginWithPwd(userLoginPwdDTO);
    }

    /**
     * 用户更改头像
     * @param file 头像图片文件
     * @return     返回图片OSS地址
     * @throws IOException 抛出IO异常
     */
    @PutMapping("/image/avatar")
    @ApiOperation("用户更改头像接口")
    public Result<String> putAvatar(@RequestParam MultipartFile file) throws IOException {
       return Result.success(userService.updateAvatar(file));
    }

    /**
     * 用户加载历史聊天标题列表
     * @return 返回聊天标题列表
     */
    @GetMapping("/ai/chat/historyRecord/list")
    @ApiOperation("用户加载历史聊天记录列表接口")
    public Result<List<String>> aiHistoryRecordTitleList(){
        // 获取用户id
        Integer userId = Integer.valueOf((String) BaseContext.get().get("USER_ID"));
        // 查询用户聊天记录标题列表
        List<String> recordTitleList = userService.historyRecordTitleList(userId);
        // 返回标题列表
        return Result.success(recordTitleList);
    }

    /**
     * 用户与ai寻医聊天
     * @param illnessMessageDTO 用户病情描述
     * @return              返回ai信息
     */
    @PostMapping("/ai/chat/illnessResolution")
    @ApiOperation("用户与ai聊天接口")
    public Result<String> aiChat(ILLNESSMessageDTO illnessMessageDTO) throws NoApiKeyException, InputRequiredException {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 如果用户时间在当前时间一分钟前或者在当前时间后，怀疑时间戳有误，报错返回
        if (illnessMessageDTO.getTimeStamp().isBefore(now.minusMinutes(1)) || illnessMessageDTO.getTimeStamp().isAfter(now)){
            return Result.error(400, MsgConstant.UserTimeError,null);
        }
        // 返回ai消息
        return Result.success(userService.chat(illnessMessageDTO));
    }

    /**
     * 用户上传具体图片，初步描述，细分科室
     *
     * @param image 图片
     * @param desc  初步描述
     */
    @PostMapping("/ai/chat/start")
    @ApiOperation("科室细分图片与初步描述接口")
    public Result<String> aiDepartmentSortByImage(@RequestParam MultipartFile image, @RequestParam String desc) {
        // 如果用户输入病情描述为空报错返回
        if (desc == null){
            return Result.error(400,MsgConstant.IllnessDescNullError);
        }
        // 获取对话标题，初始化对话
        String departmentSubdivision = userService.aiDepartmentSortByImage(image,desc);
        return Result.success(departmentSubdivision);
    }
//
//    public

    //检查手机号码格式
    private boolean checkPhone(String phone) {
        String regex = "^1\\d{10}$";
        boolean isValid = Pattern.matches(regex, phone);
        if (!isValid){
            log.info("{}手机号不符合大陆手机号格式", phone);
            return false;
        }
        return true;
    }


}
