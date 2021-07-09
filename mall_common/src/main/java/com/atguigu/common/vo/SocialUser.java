package com.atguigu.common.vo;

import lombok.Data;

/**
 * @Description: 社交用户信息
 * @author: zhangshuaiyin
 * @createTime: 2021-04-22 19:07
 **/
@Data
public class SocialUser {

    private String access_token;

    private String remind_in;

    private Long expires_in;

    private String uid;

    private String isRealName;


}