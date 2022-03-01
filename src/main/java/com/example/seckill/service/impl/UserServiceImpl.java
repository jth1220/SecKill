package com.example.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.seckill.exceptoin.GlobalException;
import com.example.seckill.mapper.UserMapper;
import com.example.seckill.pojo.User;
import com.example.seckill.service.IUserService;
import com.example.seckill.utils.CookieUtil;
import com.example.seckill.utils.MD5Util;
import com.example.seckill.utils.UUIDUtil;
import com.example.seckill.vo.LoginVo;
import com.example.seckill.vo.RespBean;
import com.example.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author JiangTH
 * @since 2022-01-03
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 登陆
     * @param loginVo
     * @param request
     * @param response
     * @return
     */
    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
//        if(StringUtils.isEmpty(mobile)||StringUtils.isEmpty(password)){
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
//        }
//        if(!ValidatorUtil.isMobile(mobile)){
//            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
//        }
        //根据手机号码获取用户名
        User user = userMapper.selectById(mobile);
        if(user==null){
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //判断密码是否正确
        if(!MD5Util.fromPassToDBPass(password,user.getSlat()).equals(user.getPassword())){
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //生成cookie
        String ticket = UUIDUtil.uuid();
//        request.getSession().setAttribute(ticket,user);
        redisTemplate.opsForValue().set("user"+ticket,user);
        CookieUtil.setCookie(request,response,"userTicket",ticket);
        return RespBean.success(ticket);
    }

    @Override
    public User getUserByCookie(String userTicker,HttpServletResponse response,HttpServletRequest request) {
        if(StringUtils.isEmpty(userTicker)){
            return null;
        }
        User user = (User)redisTemplate.opsForValue().get("user" + userTicker);
        if(user!=null){
            CookieUtil.setCookie(request,response,"userTicket",userTicker);
        }
        return user;
    }

    /**
     * 更新密码
     *
     * @param userTicket
     * @param id
     * @param password
     * @param request
     * @param response
     * @return
     */
    @Override
    public RespBean updatePassword(String userTicket, Long id, String password,
                                   HttpServletRequest request, HttpServletResponse response) {
        User user = getUserByCookie(userTicket, response, request);
        if(user==null){
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
        }
        user.setPassword(MD5Util.inputPassToDBPass(password,user.getSlat()));
        int i = userMapper.updateById(user);
        if(i==1){
            redisTemplate.delete("user:"+userTicket);
            return RespBean.success();
        }
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }
}
