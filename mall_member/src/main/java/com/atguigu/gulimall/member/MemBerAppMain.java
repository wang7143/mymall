package com.atguigu.gulimall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.atguigu.gulimall.member.dao")
@SpringBootApplication
public class MemBerAppMain {
    public static void main(String[] args) {
        SpringApplication.run(MemBerAppMain.class, args);
    }
}
