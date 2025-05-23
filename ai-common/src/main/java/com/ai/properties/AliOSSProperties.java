package com.ai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ali.oss")
public class AliOSSProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String endPoint;
    private String region;
}
