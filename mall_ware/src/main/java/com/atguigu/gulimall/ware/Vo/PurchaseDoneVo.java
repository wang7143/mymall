package com.atguigu.gulimall.ware.Vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PurchaseDoneVo {

    @NotNull
    private Long id;

    private List<PurchaseItemVo> items;


}
