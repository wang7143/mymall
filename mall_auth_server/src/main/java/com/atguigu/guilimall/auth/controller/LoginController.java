package com.atguigu.guilimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberEntity;
import com.atguigu.common.vo.UserLoginVo;
import com.atguigu.guilimall.auth.Vo.UserRegisVo;
import com.atguigu.guilimall.auth.feign.MemberFeignService;
import com.atguigu.guilimall.auth.feign.Sms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    /**
     * SpringMVC viewcontroller 直接映射
     * @return
     */

//    @GetMapping("/login.html")
//    public String loginPage(){
//        return "login";
//    }
//
//    @GetMapping("/reg.html")
//    public String regPage(){
//        return "reg";
//    }
    @Autowired
    Sms sms;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone){

        String rediscode = redisTemplate.opsForValue().get("sms:code:" + phone);
        if(!StringUtils.isEmpty(rediscode)){
            long l = Long.parseLong(rediscode.split("_")[1]);
            if(System.currentTimeMillis() - l < 60000){
                return R.error(10002,"稍后再试");
            }
        }

        String code = UUID.randomUUID().toString().substring(0, 5);
        String codetoredis = code + "_" + System.currentTimeMillis();

        System.out.println("SMS_code"+code);
        //redis缓存验证码，防止同一个手机号在60秒内再次发送
        redisTemplate.opsForValue().set("sms:code:"+phone,codetoredis,10, TimeUnit.MINUTES);

        sms.sendCode(phone,code);
        return R.ok();
    }

    /**
     * RedirectAttributes 模拟重定向带数据 利用session 存放数据，跳转后取出数去就会删除
     * @param vo
     * @param result
     * @param model
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegisVo vo, BindingResult result, Model model, RedirectAttributes redirectAttributes){
        if(result.hasErrors()){

            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField,FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.mall.com/reg.html";
        }
        //真正注册 调用远程服务
        //1.校验验证码
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get("sms:code:" + vo.getPhone());
        if(!StringUtils.isEmpty(s)){
            if(code.equals(s.split("_")[0])){
                //验证码通过
                redisTemplate.delete("sms:code:" + vo.getPhone());
                //远程注册
                R r = memberFeignService.regist(vo);
                if(r.getCode() == 0){
                    //成功

                    return "redirect:http://auth.mall.com/login.html";
                }else{
                    Map<String,String> errors = new HashMap<>();
                    errors.put("msg",r.getData(new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.mall.com/reg.html";
                }

            }else{
                Map<String,String> errors = new HashMap<>();
                errors.put("code","验证码错误");
                redirectAttributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.mall.com/reg.html";
            }
        }else{
            Map<String,String> errors = new HashMap<>();
            errors.put("code","验证码错误");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.mall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session){
        //远程登录
        R login = memberFeignService.login(vo);
        MemberEntity data = login.getData("data", new TypeReference<MemberEntity>() {
        });
        if(login.getCode() == 0){
            session.setAttribute("loginUser",data);
            return "redirect:http://mall.com";
        }else{
            Map<String,String> errors = new HashMap<>();
            errors.put("msg",login.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.mall.com/login.html";
        }
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){

        Object loginUser = session.getAttribute("loginUser");
        if(loginUser == null){

            return "login";
        }else{
            return  "redirect:http://mall.com";
        }

    }
}
