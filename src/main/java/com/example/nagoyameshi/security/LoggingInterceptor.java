package com.example.nagoyameshi.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. ログイン中のユーザー情報を取得（SecurityContextHolderから直接取れる）
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();
            MDC.put("userId", String.valueOf(user.getUser().getId()));
        } else {
            MDC.put("userId", "guest"); // 未ログイン時はguestなど
        }
        return true; // 次の処理（コントローラー）に進む
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 2. 処理が終わったら自動でクリア
        MDC.clear();
    }
}