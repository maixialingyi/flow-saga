package com.flow.saga;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@MapperScan("com.flow.saga.repository.mapper")
public class Application {
    public static void main(String[] args) {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>开始启动上下文<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        SpringApplication.run(Application.class, args);
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>启动完毕<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }
}
