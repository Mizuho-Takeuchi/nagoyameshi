package com.example.nagoyameshi.security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.nagoyameshi.service.UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
	private final UserService userService;
	private final Logger log = LoggerFactory.getLogger(WebSecurityConfig.class);
	
	public WebSecurityConfig(@Lazy UserService userService) {
		this.userService = userService;
	}
	
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/css/**", "/images/**", "/js/**", "/storage/**", "/","/signup/**").permitAll()  // すべてのユーザーにアクセスを許可するURL
                .requestMatchers("/restaurants/**", "/company", "/terms").hasAnyRole("ANONYMOUS","FREE_MEMBER","PAID_MEMBER")
                .requestMatchers("/restaurants/{restaurantId}/reviews/**", "/reservations/**", "/restaurants/{restaurantId}/reservations/**", "/favorites/**", "/restaurants/{restaurantId}/favorites/**").hasAnyRole("FREE_MEMBER", "PAID_MEMBER")
                .requestMatchers("/subscription/register","/subscription/create").hasRole("FREE_MEMBER")
                .requestMatchers("/subscription/edit","/subscription/update","/subscription/cancel","/subscription/delete").hasRole("PAID_MEMBER")
                .requestMatchers("/admin/**").hasRole("ADMIN")              
                .anyRequest().authenticated()                   // 上記以外のURLはログインが必要（会員または管理者のどちらでもOK）
            )
            .formLogin((form) -> form
                .loginPage("/login")              // ログインページのURL
                .loginProcessingUrl("/login")     // ログインフォームの送信先URL
                .defaultSuccessUrl("/?loggedIn")  // ログイン成功時のリダイレクト先URL
                .failureUrl("/login?error")       // ログイン失敗時のリダイレクト先URL
                //ログイン成功ログ出力
                .successHandler((request, response, authentication) -> {
                	String email = request.getParameter("username");
                	userService.setFailedAttemptZero(email);
                	
                    log.info("Login successful: user={}", authentication.getName());
                    response.sendRedirect("/?loggedIn"); // 成功時のリダイレクト先を指定
                })
                
                //ログイン失敗
                .failureHandler((request, response, exception) -> {
                    //エラーを分ける
                    String errorParam = "";
                    String email = request.getParameter("username");
                    
                    if (exception instanceof org.springframework.security.authentication.LockedException) {
                        // アカウントロック中の場合
                        log.warn("Login failed: User {} is locked.", email);
                        errorParam = "locked";
                    } else {
                        // パスワード間違いの場合
                        if (email != null) {
                            userService.lockUser(email);
                        }
                        log.warn("Login failed: reason={}", exception.getMessage());
                        errorParam = "badCredentials";
                    }
                    
                    response.sendRedirect("/login?error="+errorParam);
                })
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutSuccessUrl("/?loggedOut")  // ログアウト時のリダイレクト先URL
                //ログアウト成功ログ出力
                .addLogoutHandler((request, response, authentication) -> {
                    if (authentication != null) {
                        log.info("Logout successful: user={}", authentication.getName());
                    }
                })
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}