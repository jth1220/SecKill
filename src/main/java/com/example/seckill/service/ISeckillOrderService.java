package com.example.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.seckill.pojo.SeckillOrder;
import com.example.seckill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author JiangTH
 * @since 2022-01-05
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {

    Long getResult(User user, Long goodsId);
}
