package com.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ai")
//@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
//@EnableScheduling //开启springTask，启动定时任务处理

public class AiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
        log.info("server started");
    }
}
