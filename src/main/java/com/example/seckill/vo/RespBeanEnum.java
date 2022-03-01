package com.example.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum RespBeanEnum {
    SUCCESS(200,"SUCCESS"),
    ERROR(500,"服务端异常"),
    //登陆模块5002
    LOGIN_ERROR(500210,"用户名或密码错误"),
    MOBILE_ERROR(500211,"手机号码格式不正确"),
    BIND_ERROR(500212,"参数校验异常"),
    MOBILE_NOT_EXIST(500213,"用户不存在"),
    PASSWORD_UPDATE_FAIL(500214,"更新密码失败"),
    SESSION_ERROR(500215,"用户不存在"),
    //秒杀模块5005
    EMPTY_STOCK(500500,"库存不足"),
    REPEAT_ERROR(500501,"超过限购次数"),
    REQUEST_ILLEGAL(500502,"请求非法，请重新尝试"),
    CAPTCHA_ERROR(500503,"验证码错误，请重新尝试"),
    ACCESS_LIMIT_ERROR(500504,"访问过于频繁，请稍后尝试"),
    //订单模块5003
    ORDER_NOT_EXIST(500300,"订单信息不存在"),

    ;
    private final Integer code;
    private final String message;
}
