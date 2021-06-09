package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.common.vo.AttrRespVo;
import com.atguigu.common.vo.AttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-04-14 14:09:25
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);


    List<AttrEntity> getRelationAttr(Long attrgroupId);


    void saveAttr(AttrVo attr);


    PageUtils queryBaseAttr(Map<String, Object> params, Long categoryId, String type);


    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attr);



    void deleteRelation(AttrGroupRelationVo[] vos);

    PageUtils getNoRelation(Long attrgroupId, Map<String, Object> params);


    List<Long> selectSearchAttr(List<Long> attrIds);


}

