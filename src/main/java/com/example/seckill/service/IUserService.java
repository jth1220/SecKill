package com.example.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.seckill.pojo.User;
import com.example.seckill.vo.LoginVo;
import com.example.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author JiangTH
 * @since 2022-01-03
 */
public interface IUserService extends IService<User> {

    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据cookie获取用户信息
     * @param userTicker
     * @return
     */
    User getUserByCookie(String userTicker, HttpServletResponse response, HttpServletRequest request);


    RespBean updatePassword(String userTicket,Long id,String password,HttpServletRequest request, HttpServletResponse response);
}
