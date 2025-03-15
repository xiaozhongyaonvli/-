package com.ai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ali-oss")
public class AliOSSProperties {
    public String accessKeyId;
    public String accessKeySecret;
    public String bucketName;
    public String endPoint;
    public String region;
}
