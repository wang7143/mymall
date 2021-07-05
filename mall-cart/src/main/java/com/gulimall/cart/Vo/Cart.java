package com.gulimall.cart.Vo;


import java.math.BigDecimal;
import java.util.List;

public class Cart {

    List<CartitemVo> items;

    private Integer countNum; //商品数量

    private Integer countType; //商品类型数量

    private BigDecimal totalAmount; //商品总价

    private BigDecimal reduce = new BigDecimal("0.00"); //减免价格

    public List<CartitemVo> getItems() {
        return items;
    }

    public void setItems(List<CartitemVo> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if(items != null && items.size()>0){
            for(CartitemVo item : items){
                count += item.getCount();
            }
        }
        return count;
    }


    public void setCountNum(Integer countNum) {
        this.countNum = countNum;
    }

    public Integer getCountType() {
        return countType;
    }

    public void setCountType(Integer countType) {
        this.countType = countType;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        // 物品总价
        if(items != null && items.size() > 0){
            for(CartitemVo item : items){
                if(item.getCheck()){
                    amount = amount.add(item.getTotalPrice());
                }
            }
        }
        //2. 减去优惠
        return amount.subtract(getReduce());
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
