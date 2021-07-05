package com.atguigu.gulimall.order.Vo;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@ToString
@Data
public class OrderSubmitVo {

    private Long addrId;
    private Integer payType;

    private String orderToken;
    private BigDecimal payPrice;
    private String note;
}
