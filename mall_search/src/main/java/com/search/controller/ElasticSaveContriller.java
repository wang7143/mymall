package com.search.controller;

import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;

import com.search.Service.ProductSaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RequestMapping("/search/save")
@RestController
public class ElasticSaveContriller {

    /**
     *
     */
    @Autowired
    ProductSaveService productSaveService;

    //上架商品
    @PostMapping("/product")
    public R productStatusUP(@RequestBody List<SkuEsModel> skuEsList) {

        try {
            productSaveService.productStatusUP(skuEsList);
        } catch (IOException e) {
            return R.error(1003,"商品上架异常");
        }
        return R.ok();
    }
}
