package com.search.Vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {

    private String keyword;   //关键字
    private Long catalog3Id;  //三级分类Id

    private String sort;  //排序方式

    private Integer hasStock;  //是否有货

    private String skuPrice; //价格区间

    private List<Long> brandId;  //品牌多选

    private List<String> attrs;  //各种属性

    private Integer pageNum = 1;  //页码

    private String _queryString;
}
