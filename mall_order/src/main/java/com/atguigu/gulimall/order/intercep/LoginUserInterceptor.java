package com.atguigu.gulimall.order.intercep;


import com.atguigu.common.vo.MemberEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static  ThreadLocal<MemberEntity> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object attribute = request.getSession().getAttribute("loginUser");
        if(attribute != null){
            loginUser.set((MemberEntity) attribute);
            return true;
        }else{
            request.getSession().setAttribute("msg","请登录");
            response.sendRedirect("http://auth.mall.com/login.html");
            return false;
        }
    }
}
