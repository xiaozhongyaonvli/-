package com.ai.config;

import com.ai.properties.AliModelProperties;
import com.ai.utils.AliModelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@EnableConfigurationProperties(AliModelProperties.class)
public class AliModelConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AliModelUtil aliModelUtil(AliModelProperties aliModelProperties){
        return new AliModelUtil(aliModelProperties);
    }
}
