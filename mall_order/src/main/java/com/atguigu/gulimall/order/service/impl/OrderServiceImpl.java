package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.FareVo;
import com.atguigu.common.vo.MemberEntity;
import com.atguigu.common.vo.MemberAddressVo;
import com.atguigu.gulimall.order.To.OrderCreateTo;
import com.atguigu.gulimall.order.Vo.*;
import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WmsFeignService;
import com.atguigu.gulimall.order.intercep.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
            //????????????????????????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> addresses = memberFeignService.getAddresses(memberEntity.getId());
            confirmVo.setAddress(addresses);
        }, executor);

        CompletableFuture<Void> getitems = CompletableFuture.runAsync(() -> {
            //2.?????????????????????????????????????????????
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

        //3.??????????????????
        Integer integration = memberEntity.getIntegration();
        confirmVo.setIntegration(integration);

        //TODO 5.????????????
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("order:token"+memberEntity.getId(),token,30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);
        CompletableFuture.allOf(getAddress,getitems).get();
        return confirmVo;
    }

    private ThreadLocal<OrderSubmitVo> SubmitVoThreadLocal = new ThreadLocal<>();

    //?????????????????????seata
    @GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        SubmitVoThreadLocal.set(vo);
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        MemberEntity entity = LoginUserInterceptor.loginUser.get();

        //???????????? ????????????
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();

        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("order:token" + entity.getId()), orderToken);
        if(result == 0L){
            //??????
            responseVo.setCode(1);
            return responseVo;
        }else{
            //??????
            OrderCreateTo order = createOrder();
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if(Math.abs(payPrice.subtract(payAmount).doubleValue()) < 0.01){
                //????????????
                saveOrder(order);
                //4.???????????????????????????
                //???????????????????????????
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSpuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(locks);
                //
                R r = wmsFeignService.orderLockStock(lockVo);
                if(r.getCode() == 0){
                    //????????????
                    responseVo.setOrder(order.getOrder());
                    responseVo.setCode(0);
                    return responseVo;
                }else{
                    //????????????
                    responseVo.setCode(3);
                    return responseVo;
                }
            }else{
                responseVo.setCode(2);
                return responseVo;
            }
        }

//        String s = redisTemplate.opsForValue().get("order:token" + entity.getId());

//        return responseVo;
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Autowired
    OrderDao orderDao;

    @Autowired
    OrderItemDao orderItemDao;

    @Autowired
    OrderItemService orderItemService;

    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder(){
        OrderCreateTo to = new OrderCreateTo();
        //1.???????????????
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);


        //2,?????????????????????
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        //3.??????????????????
        computePrice(orderEntity,orderItemEntities);
        to.setOrder(orderEntity);
        to.setOrderItems(orderItemEntities);
        return to;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        //1.??????????????????
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        Integer giftGrowth = 0;
        Integer giftIntegration = 0;
        for (OrderItemEntity entity : orderItemEntities) {
            coupon = coupon.add(entity.getCouponAmount());
            integration = integration.add(entity.getIntegrationAmount());
            promotion = promotion.add(entity.getPromotionAmount());
            total = total.add(entity.getRealAmount());
            giftGrowth += entity.getGiftGrowth();
            giftIntegration += entity.getGiftIntegration();
        }
        //????????????
        orderEntity.setTotalAmount(total);
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);

        //????????????
        orderEntity.setIntegration(giftIntegration);
        orderEntity.setGrowth(giftGrowth);

    }

    private OrderEntity buildOrder(String orderSn) {
        MemberEntity respVo = LoginUserInterceptor.loginUser.get();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(respVo.getId());
        //??????????????????
        OrderSubmitVo orderSubmitVo = SubmitVoThreadLocal.get();
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareData = fare.getData(new TypeReference<FareVo>() {
        });
        //????????????
        orderEntity.setFreightAmount(fareData.getFare());
        //???????????????
        orderEntity.setReceiverCity(fareData.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareData.getAddress().getDetailAddress());
        orderEntity.setReceiverPhone(fareData.getAddress().getPhone());
        orderEntity.setReceiverPostCode(fareData.getAddress().getPostCode());
        orderEntity.setReceiverProvince(fareData.getAddress().getProvince());
        orderEntity.setReceiverRegion(fareData.getAddress().getRegion());

        //??????????????????
        orderEntity.setStatus(0);
        orderEntity.setAutoConfirmDay(7);
        orderEntity.setDeleteStatus(0);

        return orderEntity;
    }

    //???????????????????????????
    private List<OrderItemEntity> buildOrderItems(String orderSn){
        //???????????????????????????
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if(currentUserCartItems != null && currentUserCartItems.size() > 0){
            return currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                if (itemEntity != null) {
                    itemEntity.setOrderSn(orderSn);
                }
                return itemEntity;
            }).collect(Collectors.toList());
        }
        return null;
    }

    @Autowired
    ProductFeignService productFeignService;
    //????????????????????????
    private OrderItemEntity buildOrderItem(OrderItemVo vo) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //1.????????????????????????
        //2.?????????SPU??????
        Long skuId = vo.getSkuId();
        R r = productFeignService.getSpuInfoById(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
        });

        orderItemEntity.setSpuId(data.getId());
        orderItemEntity.setSpuBrand(data.getBrandId().toString());
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setCategoryId(data.getCatalogId());

        //3.??????sku??????
        orderItemEntity.setSkuId(vo.getSkuId());
        orderItemEntity.setSkuName(vo.getTitle());
        orderItemEntity.setSkuPic(vo.getImage());
        orderItemEntity.setSkuPrice(vo.getPrice());
        String s = StringUtils.collectionToDelimitedString(vo.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(s);
        orderItemEntity.setSkuQuantity(vo.getCount());
        //4.????????????
        //5.????????????
        orderItemEntity.setGiftGrowth(vo.getPrice().intValue()*vo.getCount());
        orderItemEntity.setGiftIntegration(vo.getPrice().intValue()*vo.getCount());

        //6?????????????????????
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        //??????????????????????????????
        BigDecimal orgin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = orgin.subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getPromotionAmount()).subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);

        return orderItemEntity;
    }
}

