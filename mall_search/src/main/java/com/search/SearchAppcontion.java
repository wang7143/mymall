package com.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SearchAppcontion {
    public static void main(String[] args) {
        SpringApplication.run(SearchAppcontion.class, args);
    }
}
