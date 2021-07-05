package com.gulimall.cart.service.Imp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;

import com.gulimall.cart.Vo.Cart;
import com.gulimall.cart.Vo.CartitemVo;
import com.gulimall.cart.Vo.SkuInfoVo;
import com.gulimall.cart.Vo.UserInfoTo;
import com.gulimall.cart.feign.Productclient;
import com.gulimall.cart.interceptor.Cartinterceptor;
import com.gulimall.cart.service.CartService;
import com.netflix.hystrix.HystrixEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImp implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    Productclient productClient;

    private final String CART_PREFIX =  "mall:cart";

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public CartitemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String o = (String) cartOps.get(skuId.toString());
        if(!StringUtils.isEmpty(o)){
            CartitemVo cartitemVo = new CartitemVo();
            cartitemVo = JSON.parseObject(o, CartitemVo.class);
            cartitemVo.setCount(cartitemVo.getCount() + num);

            cartOps.put(skuId.toString(), JSON.toJSONString(cartitemVo));
            return cartitemVo;
        }
        CartitemVo cartitemVo = new CartitemVo();
        //1.远程查询当前要添加的商品信息
        CompletableFuture<Void> skuInfoTask = CompletableFuture.runAsync(() -> {
            R skuInfo = productClient.getSkuInfo(skuId);
            SkuInfoVo data = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
            });
            cartitemVo.setCheck(true);
            cartitemVo.setCount(num);
            cartitemVo.setImage(data.getSkuDefaultImg());
            cartitemVo.setTitle(data.getSkuTitle());
            cartitemVo.setSkuId(skuId);
            cartitemVo.setPrice(data.getPrice());
        },executor);

        //2.远程查询sku的组合信息
        CompletableFuture<Void> getItem = CompletableFuture.runAsync(() -> {
            List<String> values = productClient.getSkuSaleAttrValues(skuId);
            cartitemVo.setSkuAttr(values);
        }, executor);
        CompletableFuture.allOf(skuInfoTask,getItem).get();
        String s = JSON.toJSONString(cartitemVo);
        cartOps.put(skuId.toString(),s);

        return cartitemVo;
    }

    @Override
    public CartitemVo getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String str = (String) cartOps.get(skuId.toString());
        return JSON.parseObject(str, CartitemVo.class);
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {

        Cart cart = new Cart();
        UserInfoTo userInfoTo = Cartinterceptor.thread.get();
        if(userInfoTo.getUserId() != null){
            //登录
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            String tempKey = CART_PREFIX + userInfoTo.getUserKey();
//            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
            //2.如果临时购物车的数据还没进行合并
            List<CartitemVo> items = getCartItems(tempKey);
            if(items != null){
                //临时购物车有东西，需要合并
                for (CartitemVo item : items) {
                    addToCart(item.getSkuId(),item.getCount());
                }
                clearCart(tempKey);
            }
            //3.获取登录后的所有购物车的数据
            List<CartitemVo> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }else{
            //没登陆
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartitemVo> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = Cartinterceptor.thread.get();
        String cartKey = "";
        if(userInfoTo.getUserId() != null){
            cartKey = CART_PREFIX+userInfoTo.getUserId();
        }else{
            cartKey = CART_PREFIX+userInfoTo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);

        return hashOps;
    }

    private List<CartitemVo> getCartItems(String cartKey){
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if(values != null && values.size() > 0){
            List<CartitemVo> collect = values.stream().map((obj) -> {
                String str = (String)obj;
                return JSON.parseObject(str, CartitemVo.class);
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    @Override
    public void clearCart(String cartKey){
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartitemVo cartItem = getCartItem(skuId);
        cartItem.setCheck(check==1?true:false);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {

        CartitemVo cartItem = getCartItem(skuId);
        cartItem.setCount(num);

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long sku) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        cartOps.delete(sku.toString());
    }

    @Override
    public List<CartitemVo> getUserCart() {

        UserInfoTo userInfoTo = Cartinterceptor.thread.get();
        if(userInfoTo.getUserId()==null){
            return null;
        }else{
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartitemVo> cartItems = getCartItems(cartKey);
            //被选中的购物项
            List<CartitemVo> collect = cartItems.stream().filter(item -> item.getCheck()).map(item -> {
                //查询价格
                item.setPrice(productClient.getPrice(item.getSkuId()));
                return item;
            }).collect(Collectors.toList());
            return collect;
        }

    }
}
