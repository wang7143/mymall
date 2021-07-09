package com.atguigu.gulimall.ware;


import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EnableRabbit
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class WareAppmain {
    public static void main(String[] args) {
        SpringApplication.run(WareAppmain.class, args);
    }
}
