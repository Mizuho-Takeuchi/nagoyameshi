package com.example.nagoyameshi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.stripe.exception.StripeException;

//投げられたエラーを1カ所で拾ってきてログ出力
@ControllerAdvice
public class GlobalExceptionHandler {
	private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // DB接続失敗に関する例外をキャッチ
    @ExceptionHandler({CannotGetJdbcConnectionException.class, DataAccessResourceFailureException.class})
    public String handleDbConnectionError(Exception e) {
        // ERRORレベルでログを出力
        log.error("Database connection error occurred: {}", e.getMessage());
        
        // ユーザーにはトップページを表示
        return "/";
    }
    
    // Stripeの通信エラーをキャッチ
    @ExceptionHandler(StripeException.class)
    public String handleStripeException(StripeException e) {
        // ERRORレベルでログを出力
        log.error("Stripe API error occurred: status={}, code={}, message={}", 
                  e.getStatusCode(), e.getCode(), e.getMessage());

        // ユーザーにはトップページを表示
        return "/"; 
    }
    
    //ヌルポをキャッチ
    @ExceptionHandler(NullPointerException.class)
    public String handleNullPointerException(NullPointerException e) {
        log.error("NullPointerException occurred!", e);
        return "/";
    }
    
}
