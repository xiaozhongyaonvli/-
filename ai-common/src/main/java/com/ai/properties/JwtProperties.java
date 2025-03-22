package com.ai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    private String userSecretKey;
    private int userTtl;
    private String userTokenName;
}