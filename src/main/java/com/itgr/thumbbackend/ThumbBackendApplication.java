package com.itgr.thumbbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.itgr.thumbbackend.mapper")
@EnableScheduling // 启用定时任务
public class ThumbBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThumbBackendApplication.class, args);
    }

}
