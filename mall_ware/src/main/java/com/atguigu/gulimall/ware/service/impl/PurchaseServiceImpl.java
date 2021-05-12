package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.constant.WareConstant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.Vo.MergeVo;
import com.atguigu.gulimall.ware.Vo.PurchaseDoneVo;
import com.atguigu.gulimall.ware.Vo.PurchaseItemVo;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",0).or().eq("status",1)
        );

        return new PageUtils(page);
    }

    @Autowired
    PurchaseDetailService detailService;
    
    @Autowired
    PurchaseService purchaseService;

    @Transactional
    @Override
    public R mergePur(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        List<Long> items = mergeVo.getItems();
        for (Long item : items) {
            Integer status = detailService.getById(item).getStatus();
            if (status > 1 || status < 0) {
//                new Exception("订单状态异常");
                return R.error(500,"订单状态异常已分配");
//                return R.ok();
//                System.out.println("1111");
            }
        }
        if(purchaseId == null){
            PurchaseEntity purchaseEntity = new PurchaseEntity();

            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchasStatus.CREATED.getCode());

            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        }

        //TODO 确认采购单状态是0-1才可以合并
//        Integer status = purchaseService.getById(purchaseId).getStatus();
//        if( status == 1 || status == 0){


        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity entity = new PurchaseDetailEntity();
            entity.setId(i);
            entity.setPurchaseId(finalPurchaseId);
            entity.setStatus(WareConstant.PurchasDetailStatus.ASSIGNED.getCode());
            return entity;
        }).collect(Collectors.toList());

        detailService.updateBatchById(collect);

        PurchaseEntity entity = new PurchaseEntity();
        entity.setId(purchaseId);
        entity.setUpdateTime(new Date());

        this.updateById(entity);

        return R.ok();
    }

    @Override
    public void received(List<Long> ids) {
        //确认当前采购单是新建或者已分配
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.PurchasDetailStatus.CREATED.getCode() || item.getStatus() == WareConstant.PurchasDetailStatus.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item -> {
            item.setStatus(WareConstant.PurchasStatus.RECEIVED.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        //2.改变采购单的状态
        this.updateBatchById(collect);

        //3.改变采购项的状态
        collect.forEach((item)->{
            List<PurchaseDetailEntity> entity = detailService.listDetailById(item.getId());
            List<PurchaseDetailEntity> details = entity.stream().map(en -> {
                PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                detailEntity.setId(en.getId());
                detailEntity.setStatus(WareConstant.PurchasDetailStatus.BUYING.getCode());
                return detailEntity;
            }).collect(Collectors.toList());
            detailService.updateBatchById(details);
        });
    }

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public void done(PurchaseDoneVo donVo) {
        //1.改变采购单状态
        Long id = donVo.getId();

        //2.改变采购项状态
        Boolean flag = true;
        List<PurchaseItemVo> items = donVo.getItems();

        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if(item.getStatus() == WareConstant.PurchasDetailStatus.HASERROR.getCode()){
                flag = false;
                detailEntity.setStatus(item.getStatus());
            }else {
                detailEntity.setStatus(WareConstant.PurchasDetailStatus.FINISHED.getCode());
                PurchaseDetailEntity entity = detailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());
            }
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }

        detailService.updateBatchById(updates);
        //3.将成功采购的进行入库
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag?WareConstant.PurchasDetailStatus.FINISHED.getCode() : WareConstant.PurchasDetailStatus.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }
}