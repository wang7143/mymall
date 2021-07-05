import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.order.OederAppMain;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OederAppMain.class)
@Slf4j
public class mallorderTEST {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Test
    public void create(){

        DirectExchange exchange = new DirectExchange("hello-java-exchange",true,false);
        amqpAdmin.declareExchange(exchange);
        log.info("exchange创建成功");
    }

    @Test
    public void createQueue(){

        amqpAdmin.declareQueue(new Queue("java-queue",true,false,true));
        log.info("exchang队列创建成功");
    }

    @Test
    public void createBinding(){
        amqpAdmin.declareBinding(new Binding("java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java",
                null));
        log.info("binding创建成功");
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessage(){

        for (int i = 0; i < 10; i++) {
            if (i%2 == 0){
                OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
                orderReturnReasonEntity.setId(1L);
                orderReturnReasonEntity.setCreateTime(new Date());
                orderReturnReasonEntity.setName("haha" + i);
                JSON.toJSONString(orderReturnReasonEntity);
                rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",orderReturnReasonEntity);
            }else{
                OrderEntity entity = new OrderEntity();
                entity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",entity);
            }
            log.info("sendsucces");
        }
    }
}
