package com.atguigu.gulimall.order.Config;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Configuration
public class MqConfig {

    @RabbitListener(queues = "order.release.order.queue")
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单信息，准备关闭订单"+entity.getOrderSn());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    @Bean
    public Queue orderDelayQueue(){
        Map<String, Object> agrs = new HashMap<>();
        agrs.put("x-dead-letter-exchange","order-event-exchange");
        agrs.put("x-dead-letter-routing-key","order.release.order");
        agrs.put("x-message-ttl",60000);

        return new Queue("order.delay.queue", true, false, false,agrs);
    }

    @Bean
    public Queue orderReleaseQueue(){
        return new Queue("order.release.order.queue", true, false, false);
    }

    @Bean
    public Exchange orderEventExchange(){
        return new TopicExchange("order-event-exchange",true,false);
    }

    @Bean
    public Binding orderCreateBinding(){
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",null);
    }

    @Bean
    public Binding orderReeaseBinding(){
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",null);
    }
}
