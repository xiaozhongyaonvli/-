package com.ai.service.impl;

import com.ai.constant.JwtClaimsConstant;
import com.ai.constant.MsgConstant;
import com.ai.constant.RedisConstant;
import com.ai.dto.UserDTO;
import com.ai.entity.User;
import com.ai.mapper.UserMapper;
import com.ai.properties.JwtProperties;
import com.ai.result.Result;
import com.ai.service.UserService;
import com.ai.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
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

    private static String imageLocalPath;
    @Autowired
    private JwtProperties jwtProperties;

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
    public Result register(UserDTO userDTO){
        String phone = userDTO.getPhone();
        boolean flag = phoneRepeatCheck(phone);
        if (!flag)  return Result.error(409, MsgConstant.phoneRepeatError);
        //将验证码存入redis，初始校验次数为0，如果为5次则删掉该验证码防止爆破
        String codeKey = RedisConstant.PhoneCode + phone;
        Integer time = (Integer) redisTemplate.opsForHash().get(codeKey, "time");
        if (time == null || time >= 5){ //为空表示未存入过验证码，>=5验证次数过多
            redisTemplate.opsForHash().delete(codeKey);
            return Result.error(1001,"校验验证码失败");
        }
        //校验验证码
        Integer code = (Integer) redisTemplate.opsForHash().get(codeKey,"code");
        if (!code.toString().equals(userDTO.getIdentifyCode())){
            return Result.error(400,"验证码校验不通过");
        }
        //插入新用户
        User user = new User();
        BeanUtils.copyProperties(userDTO,user);
        user.setAvatarUrl(imageLocalPath);
        userMapper.insert(user);
        //生成token，并返回
        HashMap<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.userId,user.getId());
        claims.put(JwtClaimsConstant.userName, user.getName());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTTL(),claims);
        HashMap<String,String> res = new HashMap<>();
        res.put("token",token);
        return Result.success(res);
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
        log.info("{}手机号的验证码发送成功",phone);
        // 将验证码存入redis
        HashMap<String,Integer> hashMap = new HashMap<>();
        hashMap.put("code",code);
        hashMap.put("time", 0);
        redisTemplate.opsForHash().putAll(RedisConstant.PhoneCode + phone,hashMap);
        return Result.success();
    }

    /**
     * 用户登录
     * @param userDTO (手机号码+验证码  ||   手机号 + 密码)
     * @return        200成功返回token
     */
    @Override
    public Result login(UserDTO userDTO) {
        User user = new User();
        // 密码登录
        if (userDTO.getIdentifyCode() == null){
            String pwd = userDTO.getPwd();
            user =  userMapper.selectByPhone(userDTO.getPhone());
            if (!user.getPassword().equals(pwd)){
                return Result.error(401,MsgConstant.PasswordError);
            }
        }
        // 验证码登录
        else{
            Integer code = (Integer) redisTemplate.opsForHash().get(RedisConstant.PhoneCode+userDTO.getPhone(),"code");
            if (!(code.toString().equals(userDTO.getIdentifyCode()))){
                return Result.error(401,MsgConstant.CodeError);
            }
        }
        // 如果是验证码登录，获取用户信息
        if (user.getId() == null){
            user =  userMapper.selectByPhone(userDTO.getPhone());
        }
        // 生成token
        HashMap<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.userId,user.getId());
        claims.put(JwtClaimsConstant.userName, user.getName());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTTL(),claims);
        return Result.success(token);
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
