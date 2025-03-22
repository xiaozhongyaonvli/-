package com.ai.utils;

import com.ai.properties.JwtProperties;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * 使用HMAC256签名算法加密
 */
@Slf4j
@AllArgsConstructor
public class JwtUtil {

    private JwtProperties jwtProperties;
    /**
     * 生成并返回token
     * 使用HMAC256签名加密，并使用固定的秘钥
     * @param claims    负载数据
     * @return          返回token
     */
    public String createJWT(Map<String,Object> claims){
        //获取当前时间，设置过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, jwtProperties.getUserTtl());
        // 加入负载数据
        JWTCreator.Builder jwtBuilder = JWT.create();
        claims.forEach((k,v)->{
            // 判断负载数据类型
            if (v instanceof String) jwtBuilder.withClaim(k,(String) v);
            else if (v instanceof Integer) jwtBuilder.withClaim(k, (Integer) v);
            else if (v instanceof Long) jwtBuilder.withClaim(k, (Long) v);
            else if (v instanceof Boolean) jwtBuilder.withClaim(k, (Boolean) v);
            else if (v instanceof Double) jwtBuilder.withClaim(k,(Double) v);
            else if (v instanceof Date) jwtBuilder.withClaim(k, (Date) v);
            else jwtBuilder.withClaim(k,v.toString());
        });
        //使用HMAC256签名加密
        return jwtBuilder.withExpiresAt(calendar.getTime())
                .sign(Algorithm.HMAC256(jwtProperties.getUserSecretKey()));
    }

    /**
     * 验证token，通过验证返回token解析
     * @param token     token
     */
    public DecodedJWT parse(String token){
        try {
            //验证令牌，通过则解析token
            return JWT.require(Algorithm.HMAC256(jwtProperties.getUserSecretKey())).build().verify(token);
        } catch (SignatureVerificationException e){
            log.error("无效签名：{}",token);
        } catch (TokenExpiredException e){
            log.error("token:{} 过期",token);
        } catch (AlgorithmMismatchException e){
            log.error("token{}算法不一致",token);
        } catch (Exception e){
            log.error("token未知错误");
        }
        return null;
    }
}
