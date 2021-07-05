package com.atguigu.guilimall.auth.feign;

import com.atguigu.common.utils.R;
import com.atguigu.common.vo.SocialUser;
import com.atguigu.common.vo.UserLoginVo;
import com.atguigu.guilimall.auth.Vo.UserRegisVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("mall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegisVo vo);

    @PostMapping("/member/member/login")
    public R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    public R login(@RequestBody SocialUser vo) throws Exception;
}
