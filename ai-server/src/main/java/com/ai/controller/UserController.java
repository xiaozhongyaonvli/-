package com.ai.controller;

import com.ai.constant.MsgConstant;
import com.ai.dto.UserDTO;
import com.ai.result.Result;
import com.ai.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@RestController
@Slf4j
@Api(tags = "用户相关接口")
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

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
    public Result<UserDTO> register(@RequestBody UserDTO userDTO){
        boolean flag = checkPhone(userDTO.getPhone());
        if (!flag) return Result.error(400, MsgConstant.phoneFormatError);
        return userService.register(userDTO);
    }

    /**
     * 发送验证码
     * @param phone 手机号码
     * @return      400手机号格式错误
     */
    @GetMapping("/register/code")
    @ApiOperation("发送验证码接口")
    public Result sendVerifyCode(@RequestParam String phone){
        boolean flag = checkPhone(phone);
        if (!flag) return Result.error(400, MsgConstant.phoneFormatError);
        return userService.sendVerifyCode(phone);
    }
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
