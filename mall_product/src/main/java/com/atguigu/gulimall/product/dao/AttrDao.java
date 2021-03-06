package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-04-14 14:09:25
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<Long> selectSearchAttr(@Param("attrIds") List<Long> attrIds);

}
