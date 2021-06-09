//package com.atguigu.gulimall.ware.exception;
//
//import com.atguigu.common.exception.BizCodeEnume;
//import com.atguigu.common.utils.R;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@RestControllerAdvice(basePackages = "com.atguigu.gulimall.ware.controller")
//public class gulimallException {
//
//    @ExceptionHandler(value = MethodArgumentNotValidException.class)
//    public R handleException(MethodArgumentNotValidException e){
//        log.error("数据校验出现问题{},异常类型{}",e.getMessage(),e.getClass());
//        BindingResult bindingResult = e.getBindingResult();
//        Map<String,String> errorMap = new HashMap<>();
//        bindingResult.getFieldErrors().forEach((fieldError) -> {
//            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
//        });
//        return R.error(BizCodeEnume.VAILD_EXCEPTION.getCode(),e.getMessage()).put("data",errorMap);
//    }
//
//    @ExceptionHandler(value  = Throwable.class)
//    public R handleException(Throwable e){
//        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(),e.getMessage());
//    }
//
//}
