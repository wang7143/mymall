package com.atguigu.guilimall.auth.config;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberEntity;
import com.atguigu.common.vo.SocialUser;
import com.atguigu.guilimall.auth.feign.MemberFeignService;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class OauthController {

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        //1.根据code换取Token
        Map<String,String> map = new HashMap<>();
        map.put("client_id", "2077705774");
        map.put("client_secret", "40af02bd1c7e435ba6a6e9cd3bf799fd");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code", code);
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), map, new HashMap<>());

        if(response.getStatusLine().getStatusCode()==200){
            //成功获取access_token
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            //如果当前用户是第一次进网站，自动注册进来（位当前用户生成账号信息，对应指定会员）
            R login = memberFeignService.login(socialUser);
            if(login.getCode() == 0){
                MemberEntity data = login.getData(new TypeReference<MemberEntity>() {
                });
                System.out.println("登录成功，用户信息" + data);
                session.setAttribute("loginUser",data);
                return "redirect:http://mall.com";
            }else{
                return "redirect:http://auth.mall.com/login.html";
            }
        }else{
            return "redirect:http://auth.mall.com/login.html";
        }
    }
}
