package com.atguigu.gulimall.order.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@RestController
public class RabbitController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMq")
    public String sendMessage(@RequestParam(value = "num",defaultValue = "10") Integer num){
        for (int i = 0; i < num; i++) {
            if (i%2 == 0){
                OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
                orderReturnReasonEntity.setId(1L);
                orderReturnReasonEntity.setCreateTime(new Date());
                orderReturnReasonEntity.setName("haha" + i);
                JSON.toJSONString(orderReturnReasonEntity);
                rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",orderReturnReasonEntity,new CorrelationData(UUID.randomUUID().toString()));

            }else{
                OrderEntity entity = new OrderEntity();
                entity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange","hello.java22",entity,new CorrelationData(UUID.randomUUID().toString()));
            }
        }
        return "ok";
    }
}
