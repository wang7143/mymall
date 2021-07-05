package com.atguigu.gulimall.member.exception;

public class PhoneExist extends RuntimeException{

    public PhoneExist() {
        super("手机号存在");
    }
}
