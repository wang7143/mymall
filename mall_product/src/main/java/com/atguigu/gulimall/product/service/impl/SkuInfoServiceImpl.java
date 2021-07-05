package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SkuItemVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void savekuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        String catelogId = (String) params.get("catelogId");
        String brandId = (String) params.get("brandId");
        String min = (String) params.get("min");
        String max = (String) params.get("max");
        if(!StringUtils.isEmpty(key)){
            queryWrapper.and((w)->{
                w.eq("sku_id",key).or().like("sku_name",key);
            });
        }

        if(!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            queryWrapper.eq("catalog_id",catelogId);
        }

        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            queryWrapper.eq("brand_id",brandId);
        }

        if(!StringUtils.isEmpty(min)){
            queryWrapper.ge("price",min);
        }

        if(!StringUtils.isEmpty(max)){
            try{
                BigDecimal bigDecimal = new BigDecimal(max);
                if(bigDecimal.compareTo(new BigDecimal("0"))==1){
                    queryWrapper.le("price",max);
                }
            }catch (Exception e) {

            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusById(Long spuId) {

        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id",spuId));
        return list;
    }

    @Autowired
    SkuImagesService imagesService;

    @Autowired
    SpuInfoDescService DescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(()->{
            //1.sku基本信息 pms_sku_info
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        },threadPoolExecutor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            //3.获取spu销售属性组合
            List<SkuItemSaleAttrVo> SaleAtt = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(SaleAtt);
        }, threadPoolExecutor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            //4.获取spu的介绍
            SpuInfoDescEntity desc = DescService.getById(res.getSpuId());
            skuItemVo.setDesp(desc);
        }, threadPoolExecutor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            //5.获取spu规格参数信息
            List<SpuItemAttrGroupVo> attr = attrGroupService.getAttrById(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attr);
        }, threadPoolExecutor);

        //2.sku 图片信息 pms_sku_images
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = imagesService.getImagesBySkuID(skuId);
            skuItemVo.setImages(images);
        }, threadPoolExecutor);

        //等待所有任务都完成
        CompletableFuture.allOf(saleAttrFuture,descFuture,baseAttrFuture,imageFuture).get();
        return skuItemVo;
    }
  //华为 HUAWEI P40 Pro+ 麒麟990 5G  流光幻镜 套餐二 麒麟990 5G SoC芯片 5000万超感知徕卡五摄 100倍双目变焦 全网通5G
    //华为 HUAWEI P40 Pro+ 麒麟990 5G  流光幻镜 套餐一 麒麟990 5G SoC芯片 5000万超感知徕卡五摄 100倍双目变焦 全网通5G
}