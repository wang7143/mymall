package com.atguigu.gulimall.order.feign;


import com.atguigu.common.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("mall-member")
public interface MemberFeignService {


    @GetMapping("/member/memberreceiveaddress/{memberId}/addresses")
    List<MemberAddressVo> getAddresses(@PathVariable("memberId") Long memberId);
}
