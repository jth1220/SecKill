package com.example.seckill.controller;


import com.example.seckill.pojo.User;
import com.example.seckill.rabbitmq.MQSender;
import com.example.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author JiangTH
 * @since 2022-01-03
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private MQSender mqSender;
    /**
     * 用来测试的用户信息
     * @param user
     * @return
     */
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user){
        return RespBean.success(user);
    }


}

