package com.atguigu.gulimall.member.exception;

public class UsernameExist extends RuntimeException{

    public UsernameExist() {
        super("用户名存在");
    }
}
