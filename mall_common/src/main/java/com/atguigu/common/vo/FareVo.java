package com.atguigu.common.vo;

import com.atguigu.common.vo.MemberAddressVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
