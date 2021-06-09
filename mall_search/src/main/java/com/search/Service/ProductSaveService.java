package com.search.Service;

import com.atguigu.common.to.es.SkuEsModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


public interface ProductSaveService {

    boolean productStatusUP(List<SkuEsModel> skuEsList) throws IOException;

}
