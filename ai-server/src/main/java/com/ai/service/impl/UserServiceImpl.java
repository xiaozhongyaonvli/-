package com.ai.service.impl;

import com.ai.constant.JwtClaimsConstant;
import com.ai.constant.MsgConstant;
import com.ai.constant.RedisConstant;
import com.ai.dto.UserDTO;
import java.util.Random;
import com.ai.dto.UserLoginPhoneDTO;
import com.ai.dto.UserLoginPwdDTO;
import com.ai.entity.User;
import com.ai.mapper.UserMapper;
import com.ai.properties.JwtProperties;
import com.ai.result.Result;
import com.ai.service.UserService;
import com.ai.utils.AliSSMUtil;
import com.ai.utils.JwtUtil;
import com.ai.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private Environment environment;
    @Autowired
    private AliSSMUtil aliSSMUtil;

    private static String imageLocalPath;
    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private JwtUtil jwtUtil;

    // 构造器注入 Environment
    @Autowired
    public UserServiceImpl(Environment environment) {
        imageLocalPath = environment.getProperty("image.local-dir-path");
        log.info("imageLocalPath initialized: {}", imageLocalPath);
    }
    /**
     * 检查电话是否已经注册过
     * @param phone  电话号码
     * @return       通过返回code200，已存在返回code409，资源冲突
     */
    @Override
    public Result chePhoneExists(String phone) {
        boolean flag = phoneRepeatCheck(phone);
        if (!flag)  return Result.error(409, MsgConstant.phoneRepeatError);
        return Result.success();
    }


    /**
     * 用户注册
     * @param userDTO 用户提交信息
     * @return
     */
    public Result<UserLoginVO> register(UserDTO userDTO){
        //获取电话号码
        String phone = userDTO.getPhone();
        //检查电话号码是否绑定其他账号
        boolean flag = phoneRepeatCheck(phone);
        if (!flag)  return Result.error(409, MsgConstant.phoneRepeatError);
        //将验证码从redis取出，初始校验次数为0，如果为5次则删掉该验证码防止爆破
        String codeKey = RedisConstant.PhoneCode + phone;
        Integer time = (Integer) redisTemplate.opsForHash().get(codeKey, "time");
        if (time == null || time >= 5){ //为空表示未存入过验证码，>=5验证次数过多
            redisTemplate.opsForHash().delete(codeKey);
            return Result.error(1001,"校验验证码失败",null);
        }
        //校验验证码
        Integer code = (Integer) redisTemplate.opsForHash().get(codeKey,"code");
        if (!code.toString().equals(userDTO.getIdentifyCode())){
            return Result.error(400,"验证码校验不通过",null);
        }
        //插入新用户
        User user = new User();
        BeanUtils.copyProperties(userDTO,user);
        user.setAvatarUrl(imageLocalPath);
        userMapper.insert(user);
        //生成token，并返回
        HashMap<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.userId,user.getId());
        claims.put(JwtClaimsConstant.userName, user.getUserName());
        String token = jwtUtil.createJWT(claims);
        //封装返回类
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user,userLoginVO);
        userLoginVO.setToken(token);
        //返回数据
        return Result.success(userLoginVO);
    }

    /**
     * 发送验证码
     * @param phone 手机号码
     * @return      TODO
     */
    public Result sendVerifyCode(String phone){
        boolean flag = phoneRepeatCheck(phone);
        if (!flag)  return Result.error(409, MsgConstant.phoneRepeatError);
        int code = ThreadLocalRandom.current().nextInt(100000,1000000);
        //TODO 发送验证码调用阿里云api
        boolean sendFlag = aliSSMUtil.sendPhoneCode(phone, String.valueOf(code));
        if (!sendFlag){
            log.info("{}手机号的验证码发送失败",phone);
            return Result.error(409,"手机号的验证码发送失败");
        }
        log.info("{}手机号的验证码发送成功",phone);
        // 将验证码存入redis
        HashMap<String,Integer> hashMap = new HashMap<>();
        hashMap.put("code",code);
        hashMap.put("time", 0);
        redisTemplate.opsForHash().putAll(RedisConstant.PhoneCode + phone,hashMap);
        return Result.success();
    }


    /**
     * 根据电话号码+验证码登录
     * @param userLoginPhoneDTO （手机号+验证码）
     * @return   返回登录与否信息
     */
    @Override
    public Result<UserLoginVO> loginWithPhone(UserLoginPhoneDTO userLoginPhoneDTO) {
        User user = new User();
        //取出手机号码
        String phone = userLoginPhoneDTO.getPhone();;
        //取验证码，判断与传入是否一致
        Integer code = (Integer) redisTemplate.opsForHash().get(RedisConstant.PhoneCode+phone,"code");
        if (!(code.toString().equals(userLoginPhoneDTO.getIdentifyCode()))){
            //不一致，返回
            log.error("{}号码用户传入的验证码错误",phone);
            return Result.error(401,MsgConstant.CodeError);
        }
        //根据手机号查用户
        user = userMapper.selectByPhone(phone);
        //生成token
        String token = getToken(user);
        //拷贝数据，封装返回体
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user,userLoginVO);
        userLoginVO.setToken(token);
        //返回结果
        log.info("{}号码用户，手机号码登录成功",phone);
        return Result.success(userLoginVO);
    }

    /**
     * 用户登录(用户名+密码)
     * @param userLoginPwdDTO (用户名+密码)
     * @return                200成功返回token
     */
    @Override
    public Result<UserLoginVO> loginWithPwd(UserLoginPwdDTO userLoginPwdDTO) {
        //取出用户名
        String userName = userLoginPwdDTO.getUserName();
        //根据用户名查用户
        User user = userMapper.selectByUserName(userName);
        if (user == null){
            log.error("该用户不存在");
            return Result.error(400,MsgConstant.UserNotExistError,null);
        }
        if (!Objects.equals(user.getPassword(), userLoginPwdDTO.getPassword())){
            log.error("用户{}输入密码错误",userName);
            return Result.error(401,MsgConstant.PasswordError,null);
        }
        //生成token
        String token = getToken(user);
        //拷贝数据，封装返回体
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user,userLoginVO);
        userLoginVO.setToken(token);
        //返回结果
        log.info("用户：{}登录，密码登录成功",userName);
        return Result.success(userLoginVO);
    }

    private String getToken(User user) {
        // 生成token
        HashMap<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.userId, user.getId());
        claims.put(JwtClaimsConstant.userName, user.getUserName());
        return jwtUtil.createJWT(claims);
    }

    //检测电话号是否注册过其他账号
    private boolean phoneRepeatCheck(String phone) {
        User user = userMapper.selectByPhone(phone);
        if (user != null) {
            log.info("{}手机号已经绑定其他账号，不可用于注册", phone);
            return false;
        }
        log.info("{}手机号码查重通过", phone);
        return true;
    }
}
