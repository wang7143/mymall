package com.atguigu.gulimall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@MapperScan("com.atguigu.gulimall.coupon.dao")
@SpringBootApplication
@EnableDiscoveryClient
public class CouPonAppMain {
    public static void main(String[] args) {
        SpringApplication.run(CouPonAppMain.class, args);
    }
}
