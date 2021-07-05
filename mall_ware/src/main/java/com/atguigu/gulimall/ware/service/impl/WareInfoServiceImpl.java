package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberAddressVo;
import com.atguigu.common.vo.FareVo;
import com.atguigu.gulimall.ware.feign.MemberFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareInfoDao;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;
import com.atguigu.gulimall.ware.service.WareInfoService;
import org.springframework.util.StringUtils;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.eq("id",key).or().like("name",key).or().like("address",key).or().like("areacode",key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Autowired
    MemberFeignService memberFeignService;

    @Override
    public FareVo getFare(Long id) {

        FareVo fareVo = new FareVo();
        R info = memberFeignService.info(id);
        MemberAddressVo data = info.getData("memberReceiveAddress",new TypeReference<MemberAddressVo>() {
        });
        if(data != null){
            String phone = data.getPhone();
            String s = phone.substring(phone.length() - 1, phone.length());
            fareVo.setAddress(data);
            fareVo.setFare(new BigDecimal(s));
            return fareVo;
        }
        return  null;
    }

}