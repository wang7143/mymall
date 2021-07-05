package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.FareVo;
import com.atguigu.common.vo.MemberEntity;
import com.atguigu.common.vo.MemberAddressVo;
import com.atguigu.gulimall.order.To.OrderCreateTo;
import com.atguigu.gulimall.order.Vo.OrderConfirmVo;
import com.atguigu.gulimall.order.Vo.OrderItemVo;
import com.atguigu.gulimall.order.Vo.OrderSubmitVo;
import com.atguigu.gulimall.order.Vo.SubmitOrderResponseVo;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.intercep.LoginUserInterceptor;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberEntity memberEntity = LoginUserInterceptor.loginUser.get();
        CompletableFuture<Void> getAddress = CompletableFuture.runAsync(() -> {
            //远程查询所有收获地址列表
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> addresses = memberFeignService.getAddresses(memberEntity.getId());
            confirmVo.setAddress(addresses);
        }, executor);

        CompletableFuture<Void> getitems = CompletableFuture.runAsync(() -> {
            //2.远程查询购物车所有选中的购物项
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);
        }, executor).thenRunAsync(()->{
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R skuHasStock = wmsFeignService.getSkuHasStock(collect);
            List<SkuHasStockVo> data = skuHasStock.getData(new TypeReference<List<SkuHasStockVo>>(){});
            if(data != null){
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
                confirmVo.setStocks(map);
            }
        },executor);

        //3.查询用户积分
        Integer integration = memberEntity.getIntegration();
        confirmVo.setIntegration(integration);

        //TODO 5.防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("order:token"+memberEntity.getId(),token,30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);
        CompletableFuture.allOf(getAddress,getitems).get();
        return confirmVo;
    }

    private ThreadLocal<OrderSubmitVo> SubmitVoThreadLocal = new ThreadLocal<>();

    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {

        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        MemberEntity entity = LoginUserInterceptor.loginUser.get();

        //删除令牌 原子操作
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();

        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("order:token" + entity.getId()), orderToken);
        if(result == 0L){
            //失败
            responseVo.setCode(1);
            return responseVo;
        }else{
            //成功
            OrderCreateTo order = createOrder();

        }

        String s = redisTemplate.opsForValue().get("order:token" + entity.getId());

        return responseVo;
    }

    private OrderCreateTo createOrder(){
        OrderCreateTo to = new OrderCreateTo();
        //1.生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        //获取收货地址
        OrderSubmitVo orderSubmitVo = SubmitVoThreadLocal.get();
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareData = fare.getData(new TypeReference<FareVo>() {
        });

        orderEntity.setFreightAmount(fareData.getFare());
        orderEntity.setReceiverCity(fareData.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareData.getAddress().getDetailAddress());
        orderEntity.setReceiverPhone(fareData.getAddress().getPhone());
        orderEntity.setReceiverPostCode(fareData.getAddress().getPostCode());
        orderEntity.setReceiverProvince(fareData.getAddress().getProvince());
        orderEntity.setReceiverRegion(fareData.getAddress().getRegion());

        //3,获取所有订单项
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if(currentUserCartItems != null && currentUserCartItems.size() > 0){
            currentUserCartItems.stream().map(cartItem->{
                OrderItemEntity itemEntity = new OrderItemEntity();

                return itemEntity;
            }).collect(Collectors.toList());
        }
        return to;
    }



}