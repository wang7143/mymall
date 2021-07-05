package com.atguigu.gulimall.order.Config;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class feignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //1.RequestContextHolder 拿到刚进来的这个请求
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(requestAttributes != null){
                    HttpServletRequest request = requestAttributes.getRequest();
                    //2.同步请求数据 Cookie
                    String cookie = request.getHeader("Cookie");
                    //给新请求同步了老请求的cookie
                    requestTemplate.header("Cookie",cookie);
                }
            }
        };
    }
}
