package com.ai.config;

import com.ai.properties.AliOSSProperties;
import com.ai.utils.AliOSSUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@EnableConfigurationProperties(AliOSSProperties.class)
public class AliOSSConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AliOSSUtil aliOSSUtil(AliOSSProperties aliOSSProperties){
        log.info("开始创建阿里云文件上传工具类对象： {}", aliOSSProperties);
        return new AliOSSUtil(aliOSSProperties.getEndPoint(), aliOSSProperties.getAccessKeyId()
                , aliOSSProperties.getAccessKeySecret(), aliOSSProperties.getBucketName());
    }
}
