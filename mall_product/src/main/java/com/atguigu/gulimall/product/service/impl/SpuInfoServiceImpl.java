package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.to.SkureductionTo;
import com.atguigu.common.to.SpuBoundTo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Autowired
    SpuImagesService imagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService attrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;
//nohup jstatd -p 1099 -J-Djava.security.policy=/data/jdk11/jdk-11.0.7/jstatd.all.policy -J-Djava.rmi.server.hostname=172.30.77.43 &
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1、 保存基本信息 `pms_spu_info`
        SpuInfoEntity InfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,InfoEntity);
        InfoEntity.setCreateTime(new Date());
        InfoEntity.setUpdateTime(new Date());
        this.savebaseSpuInfo(InfoEntity);

        //2、 保存描述图片 `pms_spu_info_desc`
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity InfoDescEntity = new SpuInfoDescEntity();
        InfoDescEntity.setSpuId(InfoEntity.getId());
        InfoDescEntity.setDecript(String.join(",",decript));
        spuInfoDescService.saveSpuInfoDesc(InfoDescEntity);

        //3、 保存spu图片集 `oms_spu_image`
        List<String> images = vo.getImages();
        imagesService.saveImages(InfoEntity.getId(), images);

        //4、 保存spu的规格参数 `pms_product_attr_value`
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity id = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(InfoEntity.getId());

            return valueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveProdeuctAttr(collect);
        //5 保存spu的积分信息
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(InfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if(r.getCode() != 0){
            log.error("远程保存spu积分信息失败");
        }

        //5、保存当前spu对应的所有sku信息

        // 5.1、 sku的基本信息 pms_sku_info
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                String defaultImg = "";
                for(Images image : item.getImages()) {
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(InfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(InfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(InfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);

                skuInfoService.savekuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity->{
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                // 5.2 sku的图片信息pms_sku_images
                skuImagesService.saveBatch(imagesEntities);


                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleEntites = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);
                    return attrValueEntity;
                }).collect(Collectors.toList());
                // 5.3  sku的销售属性pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleEntites);
                //5.4、 sku的优惠，满减信息 gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkureductionTo skureductionTo = new SkureductionTo();
                BeanUtils.copyProperties(item, skureductionTo);
                skureductionTo.setSkuId(skuId);
                if(skureductionTo.getFullCount() > 0 || skureductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    R r1 = couponFeignService.saveSkuRediction(skureductionTo);
                    if(r1.getCode() != 0){
                        log.error("远程保存sku信息失败");
                    }
                }

            });
        }







    }

    @Override
    public void savebaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        String status = (String) params.get("status");
        String brandId = (String) params.get("brandId");
        String catalogId = (String) params.get("catalogId");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w->{
                w.eq("id",key).or().like("spu_name",key);
            }));
        }
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }
        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }
        if(!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)){
            wrapper.eq("catalog_id",catalogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Autowired
    BrandService brandService;
    
    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public void up(Long spuId) {

        //1.查出当前spui对应的所有sku信息；
        List<SkuInfoEntity> skus = skuInfoService.getSkusById(spuId);
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        List<ProductAttrValueEntity> baseAttrs = attrValueService.baseAttrListforspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        List<Long> searchAttrIds = attrService.selectSearchAttr(attrIds);

        Set<Long> idSet = searchAttrIds.stream().collect(Collectors.toSet());

        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs1);
            return attrs1;
        }).collect(Collectors.toList());

        //TODO 1.发送远程调用，库存系统拆线呢是否有库存
        Map<Long, Boolean> stockMap = null;
        try{
            R skuHasStock = wareFeignService.getSkuHasStock(skuIdList);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {};
            stockMap = skuHasStock.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
        }catch(Exception e){
            log.error("库存调用异常：{}",e);
        }

        //2.封装每个sku信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> collect = skus.stream().map(sku -> {
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, esModel);
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());

            //是否有库存
            if(finalStockMap == null){
                esModel.setHasStock(true);
            }else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            //TODO 2.热度评分
            esModel.setHotScore(0L);
            //TODO 3.查询品牌和分类的名字信息
            BrandEntity brand = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brand.getName());
            esModel.setBrandImg(brand.getLogo());

            CategoryEntity category = categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(category.getName());

            //设置检索属性
            esModel.setAttrs(attrsList);

            //TODO 4.查询当前sku所有可以被用来检索的属性

            return esModel;
        }).collect(Collectors.toList());

        //TODO 5.将数据发送给ES进行保存；mall-search
        R r = searchFeignService.productStatusUP(collect);

        if (r.getCode() == 0){
            //TODO 6.修改spu状态已上架
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.spu_up.getCode());
        }else{
            //TODO 7.重试机制
        }
    }

    @Autowired
    SpuInfoDescService spuInfoDescService;



}