package com.example.nagoyameshi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.nagoyameshi.security.LoggingInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final LoggingInterceptor loggingInterceptor;

    public WebMvcConfig(LoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // すべてのパス（/**）に対してインターセプターを適用する
        registry.addInterceptor(loggingInterceptor).addPathPatterns("/**");
    }
}