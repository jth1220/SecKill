package com.example.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.seckill.pojo.SeckillOrder;
import com.example.seckill.service.ISeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private ISeckillOrderService seckillOrderService;
    /**
     * 测试页面跳转
     * @param model
     * @return
     */
    @RequestMapping("/hello")
    public String hello(Model model){
        model.addAttribute("name","example");
        SeckillOrder seckillOrderServiceOne = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", 18012345678L));
        System.out.println(seckillOrderServiceOne);
        return "hello";

    }

}
