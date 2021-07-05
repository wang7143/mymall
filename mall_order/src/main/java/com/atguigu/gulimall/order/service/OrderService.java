package com.atguigu.gulimall.order.service;

import com.atguigu.gulimall.order.Vo.OrderConfirmVo;
import com.atguigu.gulimall.order.Vo.OrderSubmitVo;
import com.atguigu.gulimall.order.Vo.SubmitOrderResponseVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-04-12 14:07:05
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    //订单确认页数据
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);
}

