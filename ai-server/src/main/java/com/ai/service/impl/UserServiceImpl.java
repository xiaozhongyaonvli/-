package com.ai.service.impl;

import com.ai.constant.AliOSSConstant;
import com.ai.constant.JwtClaimsConstant;
import com.ai.constant.MsgConstant;
import com.ai.constant.RedisConstant;
import com.ai.context.BaseContext;
import com.ai.dto.ILLNESSMessageDTO;
import com.ai.dto.UserDTO;
import com.ai.dto.UserLoginPhoneDTO;
import com.ai.dto.UserLoginPwdDTO;
import com.ai.entity.User;
import com.ai.entity.UserHistoryChatRecord;
import com.ai.mapper.UserHistoryChatRecordMapper;
import com.ai.mapper.UserMapper;
import com.ai.properties.ImageProperties;
import com.ai.properties.JwtProperties;
import com.ai.result.Result;
import com.ai.service.UserService;
import com.ai.utils.AliModelUtil;
import com.ai.utils.AliOSSUtil;
import com.ai.utils.AliSSMUtil;
import com.ai.utils.JwtUtil;
import com.ai.vo.MessageVO;
import com.ai.vo.UserChatRecordVO;
import com.ai.vo.UserLoginVO;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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
    @Autowired
    private UserHistoryChatRecordMapper userHistoryChatRecordMapper;
    @Autowired
    private ImageProperties imageProperties;
    @Autowired
    private AliOSSUtil aliOSSUtil;

    private static String imageLocalPath;
    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AliModelUtil aliModelUtil;

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
        // 发送验证码调用阿里云api
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
        String userName = userLoginPwdDTO.getUsername();
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

    /**
     * 与ai进行聊天
     * @param illnessMessageDTO 用户消息
     * @return                  返回ai响应信息
     */
    @Override
    public Result<StreamingResponseBody> chat(ILLNESSMessageDTO illnessMessageDTO, HttpServletResponse response) throws NoApiKeyException, InputRequiredException {
        // ai提示词
        String systemContent = "你是一位经验丰富的"+illnessMessageDTO.getDepartment()+"医生，请进行与患者沟通交流";
        // 获取ai响应结果
        String aiAnswer = aliModelUtil.chatAi(systemContent,illnessMessageDTO.getContent());
        // 用户聊天消息
        MessageVO userMessageVO = MessageVO.builder()
                .role(MessageVO.user)
                .content(illnessMessageDTO.getContent())
                .build();
        // ai聊天消息
        MessageVO aiMessageVO = MessageVO.builder()
                .role(MessageVO.assistant)
                .content(aiAnswer)
                .build();
        // redis Key
        String key = RedisConstant.userIdSessionName+BaseContext.get().get(JwtClaimsConstant.userId).toString()+illnessMessageDTO.getSessionName();
        // redis hashKey
        String hashKey = RedisConstant.messageVOList;
        // 存储的消息列表
        UserChatRecordVO userChatRecordVO = (UserChatRecordVO) redisTemplate.opsForHash().get(key,hashKey);
        List<MessageVO> messageVOList = userChatRecordVO.getMessageVOList();
        // 添加消息
        messageVOList.add(userMessageVO);
        messageVOList.add(aiMessageVO);
        redisTemplate.opsForHash().put(key,hashKey,messageVOList);
        return AliModelUtil.streamData(aiAnswer,response);

    }

    /**
     * 获取用户历史聊天标题列表
     * @param userId 用户id
     * @return       返回聊天标题列表
     */
    @Override
    public List<String> historyRecordTitleList(Integer userId) {
        // 获取该用户的所有聊天
        List<UserHistoryChatRecord> userHistoryChatRecordList = userHistoryChatRecordMapper.selectByUserId(userId);
        // 如果用户没有历史聊天记录，返回空
        if (userHistoryChatRecordList.isEmpty()) return null;
        // key为会话名称 value 为 文字OSS存储地址 和 图片OSS存储地址
        Map<String,List<String>> sessionUrl = new HashMap<>();
        // 遍历聊天记录，存入数据
        userHistoryChatRecordList.stream().forEach(record -> {
            sessionUrl.put(record.getSessionName(), Arrays.asList(record.getOssTextJsonUrl() + record.getOssImageUrl()));
        });
        // 缓存到redis中
        redisTemplate.opsForHash().putAll(RedisConstant.Record + userId.toString(),sessionUrl);
        // 返回标题列表
        return userHistoryChatRecordList.stream().map(UserHistoryChatRecord::getSessionName).collect(Collectors.toList());
    }

    /**
     * 更新用户头像
     * @param file 用户上传头像图片文件
     * @return     返回OSS地址
     */
    @Override
    public String updateAvatar(MultipartFile file) throws IOException {
        // 获取当前用户id
        Integer userId = (Integer) BaseContext.get().get(JwtClaimsConstant.userId);
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        // 获取文件后缀 .jpg 等
        int lastedIndexOf = originalFilename.lastIndexOf('.');
        String extern =  originalFilename.substring(lastedIndexOf);
        // 生成随机UUID，支持同用户存储多个头像，并且防止被恶意获取其他人头像
        String UUIDStr = UUID.randomUUID().toString();
        String OssFilePath = AliOSSConstant.IMAGE_URL_PREFIX + UUIDStr + "/" + userId + extern;
        // 生成本地保存地址，并本地保存
        String filePath = imageProperties.getLocalPath()+userId+UUIDStr;
        file.transferTo(new File(filePath));
        // 上传阿里云oss
        String ossImagePath =  aliOSSUtil.UpLoad(file, OssFilePath);
        // 更新数据库用户的头像OSS地址
        userMapper.update(User.builder()
                        .id(userId)
                        .avatarUrl(ossImagePath)
                        .build());
        // 返回OSS存储地址
        return ossImagePath;
    }

    /**
     * 根据用户上传患处图片或者X光获取细分科室
     * @param image 患处图片或者X光
     * @param desc  初步描述
     * @return      对话标题 (部门分类+细分科室)
     */
    @Override
    public String aiDepartmentSortByImage(MultipartFile image, String  desc) {
        // 根据描述获取大类区分
        String department = null;
        // 系统提示词
        String systemContent = "你是一位经验丰富的医生，请根据患者的描述进行科室分类建议。你只能回答'皮肤科'或'肺部科'或'眼底科'或'心理健康科'，不要提供任何其他解释或描述。";
        try {
            // 获取部门分类结果
            department =  aliModelUtil.chatAi(systemContent, desc);
        } catch (NoApiKeyException | InputRequiredException e) {
            throw new RuntimeException(e);
        }
        // TODO 与CV交互获取细分科室
        String departmentSubdivision = "眼底";
        // 拼接对话标题
        String sessionName = department+"/"+departmentSubdivision;
        // 将图片存储到阿里云OSS上,并获取OSS存储地址
        String imageOSSPath = aliOSSUtil.UpLoad(image, AliOSSConstant.PATIENT_IMAGE + UUID.randomUUID() + BaseContext.get().get(JwtClaimsConstant.userId) + sessionName);
        // 创建对话列表
        List<MessageVO> messageVOList = new ArrayList<>();
        // 插入初始 用户描述
        MessageVO startMessageVO = MessageVO.builder()
                .role(MessageVO.user)
                .content(desc)
                .build();
        messageVOList.add(startMessageVO);
        // 封装到存储对象
        UserChatRecordVO userChatRecordVO = new UserChatRecordVO();
        userChatRecordVO.setMessageVOList(messageVOList);
        userChatRecordVO.setImageOSS(imageOSSPath);
        userChatRecordVO.setTimeStamp(LocalDateTime.now());
        // redis初始化消息列表
        redisTemplate.opsForHash().put(RedisConstant.userIdSessionName+BaseContext.get().get(JwtClaimsConstant.userId).toString()+sessionName,RedisConstant.messageVOList,userChatRecordVO);
        // 返回对话标题
        return sessionName;
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
