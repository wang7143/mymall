package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkureductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("mall-coupon")
public interface CouponFeignService {

    //只要JSON数据模型兼容，不用相同的类
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBounds);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuRediction(SkureductionTo skureductionTo);

}
