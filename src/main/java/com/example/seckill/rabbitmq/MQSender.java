package com.example.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;



//    public void send(Object msg){
//        log.info("发送消息"+msg);
//        rabbitTemplate.convertAndSend("fanoutExchange","",msg);
//    }

    /**
     * 发送秒杀信息
     * @param message
     */
    public void sendSeckillMessage(String message){
        log.info("发送消息："+ message);
        rabbitTemplate.convertAndSend("seckillExchange","seckill.message",message);
    }

}
