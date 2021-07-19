package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.gulimall.ware.Vo.OrderItemVo;
import com.atguigu.gulimall.ware.Vo.OrderVo;
import com.atguigu.gulimall.ware.Vo.WareSkuLockVo;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.exception.NoStock;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
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
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService feignService;

    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities != null && entities.size() > 0){
            wareSkuDao.addStock(skuId, wareId, skuNum);
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
            wareSkuDao.insert(wareSkuEntity);
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
            List<Long> wareId = wareSkuDao.ListWareIdJasStock(skuId);
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
                Long count = wareSkuDao.lockSkuStock(skuId,ware,hasStock.getNum());
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

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    OrderFeignService orderFeignService;

    @Override
    public void unlockStock(StockLockedTo to) {
        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        //查询数据库的锁库存的消息
        //有 证明 库存锁定OK
        //没有。库存锁定失败,库存回滚 无需解锁
        WareOrderTaskDetailEntity orderTaskDetailEntity = wareOrderTaskDetailService.getById(detailId);
        if (orderTaskDetailEntity != null) {
            //解锁 库存没毛病
            //库存工作单的id
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            //查询订单状态 订单应该是取消的状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                //ok
                OrderVo orderVo = r.getData(new TypeReference<OrderVo>(){
                });
                if (orderVo == null || orderVo.getStatus() == 4) {
                    //订单不存在||订单取消 解锁库存
                    if (orderTaskDetailEntity.getLockStatus() == 1) {
                        //当前工作单详情，状态为1(已锁定)
                        unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detailId);
                    }
                }
            } else {
                //消息拒绝重新放入队列，让别人继续消费解锁
                throw new RuntimeException("远程服务失败");
            }
        } else {
            //无需解锁 库存服务自己出现问题
        }
    }

    private void unLockStock(Long skuId, Long wareId, Integer skuNum, Long detailId) {

        wareSkuDao.unLockStock(skuId, wareId, skuNum);
        //更新库存工作单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(detailId);
        entity.setLockStatus(2);//变为已解锁
        wareOrderTaskDetailService.updateById(entity);
    }

    @Data
    class SkuWareHasStock{
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}