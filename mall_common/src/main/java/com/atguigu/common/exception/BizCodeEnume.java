package com.atguigu.common.exception;

public enum BizCodeEnume {
    UNKNOW_EXCEPTION(10000,"系统位置异常"),
    VAILD_EXCEPTION(10001,"参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002,"请稍后再试"),
    USER_EXIST(15001,"用户名已存在"),
    PHONE_EXIST(15002,"手机号已存在");
    private int code;
    private String msg;

    BizCodeEnume(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }

}
