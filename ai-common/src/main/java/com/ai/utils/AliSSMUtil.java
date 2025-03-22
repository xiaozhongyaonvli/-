package com.ai.utils;

import com.ai.properties.AliSSMProperties;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSONObject;

@AllArgsConstructor
@Slf4j
public class AliSSMUtil {

    private AliSSMProperties aliSSMProperties;

    /**
     * 发送手机验证码
     * @param phone             手机号码
     * @param verificationCode  验证码
     * @return                  true成功false失败
     */
    public boolean sendPhoneCode(String phone, String verificationCode){
        DefaultProfile profile = DefaultProfile.getProfile(aliSSMProperties.getRegionId(),aliSSMProperties.getAccessKeyId(),aliSSMProperties.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("PhoneNumbers",phone);
        request.putQueryParameter("SignName", aliSSMProperties.getSignName());
        request.putQueryParameter("TemplateCode", aliSSMProperties.getTemplateCode());
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("code",verificationCode);
        request.putQueryParameter("TemplateParam",jsonParam.toJSONString());
        boolean flag = false;
        try {
            CommonResponse response = client.getCommonResponse(request);
            JSONObject jsonAnswer = JSONObject.parseObject(response.getData());
            String code = (String) jsonAnswer.get("Code");
            if("OK".equals(code)){
                flag = true;
            }
        } catch (ClientException e) {
            log.info("发送手机{}验证码失败",phone);
        }
        return flag;
    }
}
