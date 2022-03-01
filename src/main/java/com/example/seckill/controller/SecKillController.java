package com.example.seckill.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.seckill.config.AccessLimit;
import com.example.seckill.exceptoin.GlobalException;
import com.example.seckill.pojo.*;
import com.example.seckill.rabbitmq.MQSender;
import com.example.seckill.service.IGoodsService;
import com.example.seckill.service.IOrderService;
import com.example.seckill.service.ISeckillGoodsService;
import com.example.seckill.service.ISeckillOrderService;
import com.example.seckill.utils.JsonUtil;
import com.example.seckill.vo.GoodsVo;
import com.example.seckill.vo.RespBean;
import com.example.seckill.vo.RespBeanEnum;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/seckill")
@Slf4j
public class SecKillController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MQSender mqSender;
    @Autowired
    private RedisScript<Long> script;

    private Map<Long,Boolean> EmptyStockMap=new HashMap<>();

    @RequestMapping("/doSeckill2")
    public String doSecKill2(Model model, User user, Long goodsId){
        if(user==null){
            return "login";
        }
        model.addAttribute("user",user);
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        if(goods.getStockCount()<1){
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        //判断是否重复抢购
        SeckillOrder seckillOrderServiceOne = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        if(seckillOrderServiceOne!=null){
            model.addAttribute("errmsg",RespBeanEnum.REPEAT_ERROR.getMessage());
            return "secKillFail";
        }
        Order order = orderService.seckill(user, goods);
        model.addAttribute("order",order);
        model.addAttribute("goods",goods);
        return "orderDetail";
    }

    @RequestMapping(value = "/{path}/doSeckill" ,method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(@PathVariable String path,User user, Long goodsId){
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Boolean check=orderService.checkPath(user,goodsId,path);
        if(!check) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //判断是否重复抢购
        SeckillOrder seckillOrderServiceOne = (SeckillOrder) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if(seckillOrderServiceOne!=null){
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
        }
        //内容标记，减少redis的访问
        if(EmptyStockMap.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //将存放在redis中的库存预减
        // Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        Long stock = (Long) redisTemplate.execute(script, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
        if(stock<0){
            valueOperations.increment("seckillGoods:"+goodsId);
            EmptyStockMap.put(goodsId,true);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        return RespBean.success(0);
        /*
        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
        if(goods.getStockCount()<1){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //判断是否重复抢购
        SeckillOrder seckillOrderServiceOne = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if(seckillOrderServiceOne!=null){
            return RespBean.error(RespBeanEnum.REPEAT_ERROR);
        }
        Order order = orderService.serckill(user, goods);
        return RespBean.success(order);
         */

    }

    /**
     * 获取秒杀内容
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user,Long goodsId){
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId= seckillOrderService.getResult(user,goodsId);
        return RespBean.success(orderId);


    }

    /**
     * 获取秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    @AccessLimit(second = 1,maxCount = 5,needLogin = true)
    @RequestMapping(value = "/path",method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request){
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        boolean check=orderService.checkCaptcha(user,goodsId,captcha);
        if(!check){
            return RespBean.error(RespBeanEnum.CAPTCHA_ERROR);
        }
        String str=orderService.createPath(user,goodsId);
        return RespBean.success(str);
    }

    /**
     * 验证码
     *
     */
    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    public void verifyCode(User user, Long goodsId, HttpServletResponse response) {
        if (null==user||goodsId<0){
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        // 设置请求头为输出图片类型
        response.setContentType("image/jpg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        //生成验证码，将结果放入redis
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);

        redisTemplate.opsForValue().set("captcha:"+user.getId()+":"+goodsId,captcha.text(),300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败",e.getMessage());
        }
    }

    /**
     * 系统初始化将商品数量加载到redis中
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        for (GoodsVo goodsVo : list) {
            EmptyStockMap.put(goodsVo.getId(),false);
            redisTemplate.opsForValue().set("seckillGoods:"+goodsVo.getId(),goodsVo.getStockCount());
        }
    }
}
