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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes){
        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
        //前去创建订单，
        if(responseVo.getCode() == 0){
            model.addAttribute("submitOrderResp",responseVo);
            return "pay";
        }else{
            String msg = "";
            switch (responseVo.getCode()){
                case 1: msg = "订单信息过期";break;
                case 2: msg = "订单价格发生变化";break;
                case 3: msg = "库存锁定失败，库存不足";break;
            }
            redirectAttributes.addFlashAttribute("msg",msg);
            return "redirect:http://order.mall.com/toTrade";
        }
    }
}
