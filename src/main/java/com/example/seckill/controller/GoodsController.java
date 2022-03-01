package com.example.seckill.controller;


import com.example.seckill.pojo.User;
import com.example.seckill.service.IGoodsService;
import com.example.seckill.service.IUserService;
import com.example.seckill.service.impl.UserServiceImpl;
import com.example.seckill.vo.DetailVo;
import com.example.seckill.vo.GoodsVo;
import com.example.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/goods")
public class GoodsController {


    @Autowired
    private IUserService userService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 跳转到商品列表页
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/toList",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList( Model model, User user,HttpServletRequest request,HttpServletResponse response) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        //如果redis中不为空。直接返回界面
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        //如果为空，手动渲染，存入redis并返回
        model.addAttribute("user",user);
        model.addAttribute("goodsList",goodsService.findGoodsVo());
        WebContext webContext = new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", webContext);
        if(!StringUtils.isEmpty(html)){
            valueOperations.set("goodsList",html,60, TimeUnit.SECONDS);
        }
        return html;
    }

    /**
     * 跳转商品详情页
     * @return
     */
    @RequestMapping(value = "/toDetail2/{goodsId}",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail2(Model model,User user,@PathVariable Long goodsId,
                           HttpServletResponse response,HttpServletRequest request){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //尝试从redis中获得已经缓存的数据
        String html = (String) valueOperations.get("goodsDetail" + goodsId);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        model.addAttribute("user",user);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        int secKillStatus=0;
        int remainSeconds=0;
        //秒杀还未开始
        if(nowDate.before(startDate)){
            remainSeconds= (int) ((startDate.getTime()-nowDate.getTime())/1000);
        }else if(nowDate.after(endDate)){
            secKillStatus=2;
            remainSeconds=-1;
        }else{
            secKillStatus=1;
            remainSeconds=0;
        }
        model.addAttribute("secKillStatus",secKillStatus);
        model.addAttribute("remainSeconds",remainSeconds);
        model.addAttribute("goods",goodsVo);
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", webContext);
        if(!StringUtils.isEmpty(html)){
            valueOperations.set("goodsDetail"+goodsId,html,60,TimeUnit.SECONDS);
        }
        return html;
    }


    /**
     * 跳转商品详情页
     * @return
     */
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public RespBean toDetail(Model model, User user, @PathVariable Long goodsId){
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        int secKillStatus=0;
        int remainSeconds=0;
        //秒杀还未开始
        if(nowDate.before(startDate)){
            remainSeconds= (int) ((startDate.getTime()-nowDate.getTime())/1000);
        }else if(nowDate.after(endDate)){
            secKillStatus=2;
            remainSeconds=-1;
        }else{
            secKillStatus=1;
            remainSeconds=0;
        }
        DetailVo detailVo = new DetailVo();
        detailVo.setUser(user);
        detailVo.setGoodsVo(goodsVo);
        detailVo.setSecKillStatus(secKillStatus);
        detailVo.setRemainSecond(remainSeconds);
        return RespBean.success(detailVo);
    }
}
