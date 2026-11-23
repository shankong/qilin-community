package com.qilin.system.interceptor;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qilin.system.common.Result;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 登录拦截器
 * 用于校验用户是否登录
 */
@Component 
public class LoginInterceptor  implements HandlerInterceptor {

    /**
     * 用于把错误响应转换为JSON格式
     * 添加进 Http 响应体
     */
    private  final ObjectMapper objectMapper;

    public LoginInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public boolean preHandle(
        HttpServletRequest request, 
        HttpServletResponse response, 
        Object handler) throws Exception {
        // 从session中获取用户id
        HttpSession session = request.getSession(false);

        // 如果session中存在用户id，则说明用户已登录
        if(session != null && session.getAttribute("LOGIN_USER_ID") != null){
            return true;
        }

        // 如果session中不存在用户id，则说明用户未登录
        // 返回401错误响应
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Result.error(401, "请先登录"));
        return false;
    }
}
