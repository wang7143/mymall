package com.gulimall.cart.Controller;

import com.gulimall.cart.Vo.Cart;
import com.gulimall.cart.Vo.CartitemVo;
import com.gulimall.cart.Vo.UserInfoTo;
import com.gulimall.cart.interceptor.Cartinterceptor;
import com.gulimall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class Cartcontroller {


    @ResponseBody
    @GetMapping("/currentUserCartItems")
    public List<CartitemVo> getCurrentUserCartItems(){
        return cartService.getUserCart();
    }

    /**
     *第一次使用会给一个临时身份，保存到cookie
     *
     * 登录 有session
     * 没登录：按照cookie 带来的user-keys
     * 第一次 临时身份
     * @param
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {


//        System.out.println(userInfoTo);
        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    @Autowired
    CartService cartService;

    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes ra) throws ExecutionException, InterruptedException {

        cartService.addToCart(skuId,num);

        ra.addAttribute("skuId",skuId);
        return "redirect:http://cart.mall.com/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCatrSuccessPage(@RequestParam("skuId") Long skuId,Model model){

        CartitemVo item = cartService.getCartItem(skuId);
        model.addAttribute("item",item);
        //冲定向到成功页面
        return "sucess";
    }

    @GetMapping("/checkItem.html")
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("check") Integer check) {
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.mall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String CountItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num) {
        cartService.changeItemCount(skuId,num);
        return "redirect:http://cart.mall.com/cart.html";
    }

    @GetMapping("/deleteItem")
    public String DeleteItem(@RequestParam("skuId") Long sku){

        cartService.deleteItem(sku);
        return "redirect:http://cart.mall.com/cart.html";
    }
}
