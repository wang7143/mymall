package com.atguigu.gulimall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.atguigu.gulimall.ware.dao")
@SpringBootApplication
public class WareAppmain {
    public static void main(String[] args) {
        SpringApplication.run(WareAppmain.class, args);
    }
}
