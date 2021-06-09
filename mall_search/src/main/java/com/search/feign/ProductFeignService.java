package com.search.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("mall-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/info/{attrId}")
    public R getAtrInfo(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/brand/infos")
    public R brandinfo(@RequestParam("brandId") List<Long> brandId);
}
