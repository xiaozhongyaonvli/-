package com.ai.config;

import com.ai.properties.AliSSMProperties;
import com.ai.utils.AliSSMUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
@EnableConfigurationProperties(AliSSMProperties.class)
public class AliSSMConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AliSSMUtil aliSSMUtil(AliSSMProperties aliSSMProperties){
        return new AliSSMUtil(aliSSMProperties);
    }
}
