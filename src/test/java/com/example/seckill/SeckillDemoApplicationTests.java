package com.example.seckill;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.seckill.pojo.SeckillOrder;
import com.example.seckill.service.ISeckillOrderService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class SeckillDemoApplicationTests {

    @Autowired
    private RedisScript redisScript;
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testLock01(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Boolean isLock = valueOperations.setIfAbsent("k1", "v1");
        if(isLock){
            valueOperations.set("name","xxxx");
            String name = (String) valueOperations.get("name");
            System.out.println(name);
            redisTemplate.delete("k1");
        }else{
            System.out.println("线程正在使用，请稍后");
        }
    }
    @Test
    public void testLock02(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String s = UUID.randomUUID().toString();
        Boolean isLock = valueOperations.setIfAbsent("k1",s,1200, TimeUnit.SECONDS);
        if(isLock){
            valueOperations.set("name","xxxx");
            String name = (String) valueOperations.get("name");
            System.out.println(name);
            System.out.println(valueOperations.get("k1"));
//            Boolean result = (Boolean) redisTemplate.execute(redisScript, Collections.singletonList("k1"), s);
//            System.out.println(result);
        }else{
            System.out.println("线程正在使用，请稍后");
        }
    }

}
