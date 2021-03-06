package com.atguigu.gulimall.product;


import com.aliyun.oss.OSSClient;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.SkuSaleAttrValueDao;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@SpringBootTest
public class ProDuctAppmainTest {

    @Autowired
    BrandService brandService;

    @Autowired
    OSSClient ossClient;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void test() {
//        List<SpuItemAttrGroupVo> attribute = attrGroupDao.getAttribute(13L, 225L);
//        System.out.println(attribute);
        List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId(12L);
        System.out.println(saleAttrsBySpuId);
    }

    @Test
    public void RedisClient(){
        System.out.println(redissonClient);

    }

    @Test
    public void testredis() throws Exception {
        ValueOperations<String, String> value = redisTemplate.opsForValue();

        //??????
        value.set("hello","world_" + UUID.randomUUID().toString());

        //??????
        String string = value.get("hello");
        System.out.println(string);
    }

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
        brandEntity.setDescript("????????????");
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

    @Test
    public void upload() throws FileNotFoundException {


        // ??????OSSClient?????????
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        InputStream inputStream = new FileInputStream("C:\\Users\\wang\\Desktop\\test.png");
        // ??????Bucket?????????Object???????????????Object???????????????????????????Bucket?????????
        ossClient.putObject("gongnong-mall", "test.png", inputStream);

        // ??????OSSClient???
        ossClient.shutdown();
    }


    @Test
    public void testfindPath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("????????????{}", Arrays.asList(catelogPath));
    }
}