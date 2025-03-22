package com.ai.interceptor;

import com.ai.constant.JwtClaimsConstant;
import com.ai.context.BaseContext;
import com.ai.properties.JwtProperties;
import com.ai.utils.JwtUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Autowired
    public JwtUtil jwtUtil;
    @Autowired
    public JwtProperties jwtProperties;

    /**
     * 校验jwt
     * @param request    请求
     * @param response   响应
     * @param handler    调用controller时为HandlerMethod，静态资源则不属于
     * @return           是否放行
     * @throws Exception 处理异常
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }
        //获取令牌token
        String token = request.getHeader(jwtProperties.getUserTokenName());
        //校验令牌
        try {
            log.info("jwt校验：{}", token);
            DecodedJWT decodedJWT = jwtUtil.parse(token);
            Integer userId = decodedJWT.getClaim(JwtClaimsConstant.userId).asInt();
            String userName = decodedJWT.getClaim(JwtClaimsConstant.userName).asString();
            HashMap<String, Object> map = new HashMap<>();
            map.put(JwtClaimsConstant.userId, userId);
            map.put(JwtClaimsConstant.userName, userName);
            log.info("当前用户id:{}", userId);
            log.info("当前用户名称：{}", userName);
            BaseContext.set(map);
            //放行
            return true;
        } catch (Exception e) {
            //不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }
}
