package com.atguigu.gulimall.ware.Vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVo {

    private String orderSn;

    private List<OrderItemVo> locks;



}
