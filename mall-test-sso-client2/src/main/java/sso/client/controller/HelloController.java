package sso.client.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;


@Controller
public class HelloController {

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }

    @Value("${sso.server.url}")
    String ssoServer;


    @GetMapping("/employees")
    public String employees(Model model, HttpSession session, @RequestParam(value = "token",required = false) String token){

        if(!StringUtils.isEmpty(token)){
            session.setAttribute("loginUser","zhangsan");
        }

        Object loginUser = session.getAttribute("loginUser");
        if (loginUser==null){

            return "redirect:"+ssoServer + "?redirect_url=http://client1.com:8082/employees";
        }else{
            List<String> emps = new ArrayList<String>();
            emps.add("张三");
            emps.add("李四");
            model.addAttribute("emps",emps);
            return "list";
        }
    }
}
