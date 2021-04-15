package com.atguigu.gulimall.member.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.feign.CouponFeign;
import com.atguigu.gulimall.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
public class test {

    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeign couponFeign;

    @RequestMapping("/co")
    public R test1() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("zhangsan");

        R membercoupons = couponFeign.membercoupons();

        return R.ok().put("member",memberEntity).put("coupons",membercoupons.get("coupons"));
    }
}
