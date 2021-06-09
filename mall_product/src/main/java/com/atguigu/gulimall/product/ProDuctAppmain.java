package com.atguigu.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


@MapperScan("com.atguigu.gulimall.product.dao")
@EnableCaching     //缓存
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atguigu.gulimall.product.feign")
public class ProDuctAppmain {
    public static void main(String[] args) {
        SpringApplication.run(ProDuctAppmain.class, args);
    }
}
