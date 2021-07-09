package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.atguigu.common.utils.R;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.gulimall.ware.Vo.OrderItemVo;
import com.atguigu.gulimall.ware.Vo.WareSkuLockVo;
import com.atguigu.gulimall.ware.exception.NoStock;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)){
            wrapper.eq("sku_id",skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)){
            wrapper.eq("ware_id",wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Autowired
    WareSkuDao wxSkuDao;

    @Autowired
    ProductFeignService feignService;

    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> entities = wxSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities != null && entities.size() > 0){
            wxSkuDao.addStock(skuId, wareId, skuNum);
        }else{
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            // 失败无需回滚 ，自己抓异常，方法二
            try {
                R info = feignService.info(skuId);
                Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0){
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e) {

            }
            wxSkuDao.insert(wareSkuEntity);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> vos = skuIds.stream().map(sku -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            Long count = baseMapper.getSkuHasStock(sku);
            if(count == null){

            }
            vo.setSkuId(sku);
            vo.setHasStock(count>0);

            return vo;
        }).collect(Collectors.toList());

        return vos;
    }


    //运行时异常都会回滚
    @Transactional(rollbackFor = NoStock.class)
    @Override
    public boolean orderLockStock(WareSkuLockVo vo) {

        //1.找到每个商品库存在哪
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock hasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            hasStock.setSkuId(skuId);
            List<Long> wareId = wxSkuDao.ListWareIdJasStock(skuId);
            hasStock.setWareId(wareId);
            hasStock.setNum(item.getCount());
            return hasStock;
        }).collect(Collectors.toList());

        Boolean allLock = true;

        for (SkuWareHasStock hasStock : collect){
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareId = hasStock.getWareId();
            if(wareId == null || wareId.size() == 0){
                //没有库存
                throw new NoStock(skuId);
            }
            for (Long ware : wareId){
                Long count = wxSkuDao.lockSkuStock(skuId,ware,hasStock.getNum());
                if(count == 1){
                    //当前仓库锁成功
                    skuStocked = true;
                    break;
                }else{

                }
            }

            if(skuStocked == false){
                throw new NoStock(skuId);
            }
        }

        // 3. 成功
        return true;
    }

    @Data
    class SkuWareHasStock{
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}