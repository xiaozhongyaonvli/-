package com.ai.controller;

import com.ai.constant.JwtClaimsConstant;
import com.ai.constant.MsgConstant;
import com.ai.context.BaseContext;
import com.ai.dto.UserDTO;
import com.ai.dto.UserLoginPhoneDTO;
import com.ai.dto.UserLoginPwdDTO;
import com.ai.properties.ImageProperties;
import com.ai.result.Result;
import com.ai.service.UserService;
import com.ai.utils.AliOSSUtil;
import com.ai.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
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

    @PutMapping("/image")
    @ApiOperation("用户更改头像接口")
    public Result putImage(@RequestParam MultipartFile file) throws IOException {
        // 获取当前用户id
        Integer userId = (Integer) BaseContext.get().get(JwtClaimsConstant.userId);
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        int lastedIndexOf = originalFilename.lastIndexOf('.');
        String extern =  originalFilename.substring(lastedIndexOf);
        String fileName = userId + UUID.randomUUID().toString() + extern;
        // 生成本地保存地址，并本地保存
        String filePath = imageProperties.getLocalPath()+fileName;
        file.transferTo(new File(filePath));
        //上传阿里云oss
        String ossImagePath =  aliOSSUtil.UpLoad(file, fileName);
        return Result.success(ossImagePath);
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
