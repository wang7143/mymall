package com.atguigu.gulimall.coupon.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.coupon.entity.CouponEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RefreshScope
@RestController
@RequestMapping("/coutest")
public class test {

    @Value("${coupon.user.name}")
    private String name;

    @Value("${coupon.user.age}")
    private Integer age;

    @RequestMapping("/t1")
    public R membercoupons(){
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setCouponName("满100减10");
        return R.ok().put("coupons", Arrays.asList(couponEntity));
    }

    @RequestMapping("/t2")
    public R tembercoupons() {
        return R.ok().put("name",name).put("age",age);
    }
}
