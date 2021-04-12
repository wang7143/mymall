package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


@SpringBootTest
public class ProDuctAppmainTest {

    @Autowired
    BrandService brandService;

    @Test
    public void test1(){
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("huawei");
        brandService.save(brandEntity);
        System.out.println("OK");
    }

    @Test
    public void update(){
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(1L);
        brandEntity.setDescript("修改测试");
        brandService.updateById(brandEntity);
        System.out.println("OK");
    }

    @Test
    public void test3(){
        List<BrandEntity> brand_id = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        for (BrandEntity brandEntity : brand_id) {
            System.out.println(brandEntity);
        }
    }
}