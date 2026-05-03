package com.example.nagoyameshi.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;
import com.example.nagoyameshi.event.SignupEventPublisher;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.service.UserService;
import com.example.nagoyameshi.service.VerificationTokenService;

@Controller
public class AuthController {
	private final UserService userService;
	private final SignupEventPublisher signupEventPublisher;
	private final VerificationTokenService verificationTokenService;
	private final Logger log = LoggerFactory.getLogger(AuthController.class);
	
	public AuthController(UserService userService,
						SignupEventPublisher signupEventPublisher,
						VerificationTokenService verificationTokenService) {
		this.userService = userService;
		this.signupEventPublisher = signupEventPublisher;
		this.verificationTokenService = verificationTokenService;
	}
	@GetMapping("/login")
	public String login() {
		return "auth/login";
	}
	
	@GetMapping("/signup")
	public String signup(Model model) {
		SignupForm signupForm = new SignupForm();
		model.addAttribute("signupForm", signupForm);
		return "auth/signup";
	}
	
	@PostMapping("/signup")
	public String signup(@ModelAttribute @Validated SignupForm signupForm,
					    BindingResult bindingResult,
					    RedirectAttributes redirectAttributes,
					    HttpServletRequest httpServletRequest,
					    Model model) {
		
		if(userService.isEmailRegistered(signupForm.getEmail())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
            bindingResult.addError(fieldError);
		}
		
        if (!userService.isSamePassword(signupForm.getPassword(), signupForm.getPasswordConfirmation())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
            bindingResult.addError(fieldError);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("signupForm", signupForm);

            return "auth/signup";
        }
        
        User user = userService.createUser(signupForm);
        String url = new String(httpServletRequest.getRequestURL());
        signupEventPublisher.publishSignupEvent(user, url);
        redirectAttributes.addFlashAttribute("successMessage", "ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、会員登録を完了してください。");        
        //ログ
        log.info("Starting the process: user registration.");
		return "redirect:/";
	}
	
	@GetMapping("/signup/verify")
	public String verify(@RequestParam(name = "token") String token,
						Model model) {
		//取得したtokenパラメータの値を使い、verification_tokensテーブル内を検索する
		VerificationToken verificationToken = verificationTokenService.findVerificationTokenByToken(token);
		
		//一致するデータが存在すれば、そのデータに紐づいたユーザーのenabledカラムの値をtrueにする
		if(verificationToken != null) {
			User user = verificationToken.getUser();
			userService.enableUser(user);
			model.addAttribute("successMessage", "会員登録が完了しました。");
			log.info("Successfully completed email verification: User account activated.");
		}else {
			model.addAttribute("errorMessage", "トークンが無効です。");
			log.info("Faild to verify email: Invalid verification token.");
		}
		
		return "auth/verify";
	}
}
