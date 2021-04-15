package com.atguigu.gulimall.member.feign;


import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("mall-coupon")
public interface CouponFeign {

    @RequestMapping("/coutest/t1")
    public R membercoupons();
}
