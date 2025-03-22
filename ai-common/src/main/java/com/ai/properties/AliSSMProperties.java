package com.ai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ali.ssm")
public class AliSSMProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private String endPoint;
    private String regionId;
    private String signName;
    private String templateCode;
}
