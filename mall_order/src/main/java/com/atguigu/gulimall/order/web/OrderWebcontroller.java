package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.Vo.OrderConfirmVo;
import com.atguigu.gulimall.order.Vo.OrderSubmitVo;
import com.atguigu.gulimall.order.Vo.SubmitOrderResponseVo;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebcontroller {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model, HttpServletRequest request) throws ExecutionException, InterruptedException {
        OrderConfirmVo vo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData",vo);

        return "confirm";
    }

    //提交下单
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo){
        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
        //前去创建订单，
        if(responseVo.getCode() == 0){
            return "pay";
        }else{
            return "redirect:http://order.mall.com/toTrade";
        }
    }
}
