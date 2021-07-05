package com.gulimall.cart.service;


import com.gulimall.cart.Vo.Cart;
import com.gulimall.cart.Vo.CartitemVo;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {


    CartitemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartitemVo getCartItem(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    //清空购物车
    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void changeItemCount(Long skuId, Integer num);

    void deleteItem(Long sku);

    List<CartitemVo> getUserCart();
}
