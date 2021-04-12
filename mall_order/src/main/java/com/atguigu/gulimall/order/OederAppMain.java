package com.atguigu.gulimall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.atguigu.gulimall.order.dao")
@SpringBootApplication
public class OederAppMain {
    public static void main(String[] args) {
        SpringApplication.run(OederAppMain.class, args);
    }
}
