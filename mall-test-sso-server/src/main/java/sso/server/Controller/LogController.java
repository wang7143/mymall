package sso.server.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class LogController {

    @Autowired
    StringRedisTemplate redisTemplate;

//    public String userInfo(@RequestParam("token") String token){
//
//    }

    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String url,Model model,
                            @CookieValue(value = "sso_token",required = false) String sso_token){
        if(!StringUtils.isEmpty(sso_token)){
            //有人登录过
            return "redirect:" + url + "?token=" + sso_token;
        }
        model.addAttribute("url",url);
        return "login";
    }

    @ResponseBody
    @GetMapping("/test")
    public String test(){
        return "hello";
    }


    @PostMapping("/dologin")
    public String dologin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          @RequestParam("url") String url,
                          HttpServletResponse response){
        if(!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)){
            String uuid = UUID.randomUUID().toString().replace("-","");
            redisTemplate.opsForValue().set(uuid,username);
            Cookie cookie = new Cookie("sso_token", "uuid");
            response.addCookie(cookie);
            return "redirect:" + url + "?token=" + uuid;
        }
        return "login";
    }
}
