package com.search.Service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.search.Service.ProductSaveService;
import com.search.config.ElasticConfig;
import com.search.constant.EsContant;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service("productSaveService")
public class ProductSaveServiceImp implements ProductSaveService {

    @Autowired
    RestHighLevelClient client;

    @Override
    public boolean productStatusUP(List<SkuEsModel> skuEsList) throws IOException {

        //保存到es
        //1.建立索引
        //批量保存
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel model : skuEsList){
            IndexRequest indexRequest = new IndexRequest(EsContant.PRODUCT_INDEX);
            indexRequest.id(model.getSkuId().toString());
            String s = JSON.toJSONString(model);
            System.out.println(s);
            indexRequest.source(s, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }
        System.out.println(bulkRequest.getIndices());
        BulkResponse bulk = client.bulk(bulkRequest, ElasticConfig.COMMON_OPTIONS);
        boolean b = bulk.hasFailures();
//index {[product][_doc][49], source[{"attrs":[{"attrId":2,"attrName":"上市年份","attrValue":"2021"},{"attrId":3,"attrName":"颜色","attrValue":"极光蓝"},{"attrId":9,"attrName":"电池容量","attrValue":"5000mAh"},{"attrId":10,"attrName":"机身长度(mm)","attrValue":"168"},{"attrId":12,"attrName":"CPU品牌","attrValue":"海思(Hisilicon)"}],"brandId":2,"brandImg":"https://gongnong-mall.oss-cn-beijing.aliyuncs.com/2021-05-25/a5d5ab11-7e79-4a02-8cdd-fbb43bc59d81_MQ图片.png","brandName":"华为","catalogId":225,"catalogName":"手机","hasStock":true,"hotScore":0,"saleCount":0,"skuId":49,"skuImg":"","skuPrice":2900.0000,"skuTitle":"华为500 极光蓝 12G 12GB+256GB","spuId":23}]}
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());
        log.error("商品上架错误{}",collect,bulk.toString());

        return b;
    }
}
