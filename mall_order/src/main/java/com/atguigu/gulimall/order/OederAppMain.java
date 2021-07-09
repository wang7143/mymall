package com.atguigu.gulimall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@EnableAspectJAutoProxy(exposeProxy = true)  //对外暴露代理对象
@MapperScan("com.atguigu.gulimall.order.dao")
@SpringBootApplication(scanBasePackages = {"com.atguigu.gulimall.order","com.atguigu.common.Config"})
@EnableRabbit
@EnableFeignClients
public class OederAppMain {
    public static void main(String[] args) {
        SpringApplication.run(OederAppMain.class, args);
    }
}
