package com.atguigu.gulimall.ware.service;

import com.atguigu.common.vo.FareVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-04-12 14:16:45
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVo getFare(Long id);

}

