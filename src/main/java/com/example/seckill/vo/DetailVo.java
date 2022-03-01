package com.example.seckill.vo;

import com.example.seckill.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 详情返回对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailVo {
    private User user;
    private GoodsVo goodsVo;
    private int secKillStatus;
    private int remainSecond;
}
