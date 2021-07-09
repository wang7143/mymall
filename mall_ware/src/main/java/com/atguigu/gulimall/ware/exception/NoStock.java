package com.atguigu.gulimall.ware.exception;

public class NoStock extends RuntimeException {
    private Long skuId;

    public NoStock(Long skuId){
        super("商品" + skuId + "没有足够的库存了");
    }

    public Long getSkuId(){
        return skuId;
    }

    public void setSkuId(Long skuId){
        this.skuId = skuId;
    }
}
