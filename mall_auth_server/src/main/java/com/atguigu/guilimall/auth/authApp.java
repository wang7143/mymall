package com.atguigu.guilimall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession  //整合session到redis统一存储
@SpringBootApplication(scanBasePackages = {"com.atguigu.guilimall.auth","com.atguigu.common.Config.sessionConf"})
@EnableDiscoveryClient
@EnableFeignClients
public class authApp {
    public static void main(String[] args) {
        SpringApplication.run(authApp.class, args);
    }
}
