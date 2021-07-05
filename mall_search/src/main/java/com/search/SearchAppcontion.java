package com.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication(scanBasePackages = {"com.atguigu.common.Config.sessionConf","com.search"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableRedisHttpSession
public class SearchAppcontion {
    public static void main(String[] args) {
        SpringApplication.run(SearchAppcontion.class, args);
    }
}
