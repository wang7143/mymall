package com.atguigu.gulimall.order.Vo;


import com.atguigu.common.vo.MemberAddressVo;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class OrderConfirmVo {

    @Setter @Getter
    Map<Long, Boolean> stocks;

    //收货地址
    @Setter @Getter
    List<MemberAddressVo> address;

    //所有选中的购物项
    @Setter @Getter
    List<OrderItemVo> items;

    //发票信息

    //优惠券
    @Setter @Getter
    Integer integration;

    //订单总额
//    BigDecimal total;

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if(items!=null){
            for (OrderItemVo item : items){
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    //优惠后价格
//    BigDecimal payPrice;

    public BigDecimal getPayPrice() {
        return getTotal();
    }

    //防重令牌
    @Setter @Getter
    String orderToken;

    public Integer getCount() {
        Integer i = 0;
        if(items!=null){
            for (OrderItemVo item : items){
                i += item.getCount();
            }
        }
        return i;
    }

}
