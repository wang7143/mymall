package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;


@RabbitListener(queues = {"java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

//    @RabbitListener(queues = {"java-queue"})
    @RabbitHandler
    public void receivedMessage(Message message, OrderReturnReasonEntity content, Channel channel) throws InterruptedException {
        byte[] body = message.getBody();
        //消息头属性信息
        MessageProperties messageProperties = message.getMessageProperties();
        System.out.println("内容" + content + "类型" + message.getClass());
        Thread.sleep(3000);
        System.out.println("消息打印完成" + content.getName());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println(deliveryTag);
        //签收，非批量
        try {
            if(deliveryTag%2==0){
                channel.basicAck(deliveryTag,false);
                System.out.println("货物签收。。"+deliveryTag);
            }else{
                channel.basicNack(deliveryTag,false,false);
                System.out.println("没有签收"+deliveryTag);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RabbitHandler
    public void receivedMessage2(OrderEntity content) throws InterruptedException {

        //消息头属性信息
        System.out.println("消息打印完成" + content);
    }
}