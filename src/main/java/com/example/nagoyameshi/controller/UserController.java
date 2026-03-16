package com.example.nagoyameshi.controller;

import java.time.format.DateTimeFormatter;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {
	private UserService userService;
	
	public UserController (UserService userService) {
		this.userService = userService;
	}
	
	@GetMapping
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						Model model){
		model.addAttribute("user", userDetailsImpl.getUser());
		return "user/index";
	}
	
	@GetMapping("/edit")
	public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						Model model) {
		User user = userDetailsImpl.getUser();
		String birthday;
		
		if(user.getBirthday() == null) {
			birthday = null;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		birthday = user.getBirthday().format(formatter);
		
		UserEditForm userEditForm = new UserEditForm(user.getName(),
													user.getFurigana(),
													user.getPostalCode(),
													user.getAddress(),
													user.getPhoneNumber(),
													birthday,
													user.getOccupation(),
													user.getEmail());
		
		model.addAttribute("userEditForm", userEditForm);
		
		return "user/edit";
	}
	
	@PostMapping("/update")
	public String update(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						@ModelAttribute @Validated UserEditForm userEditForm,
						BindingResult bindingResult,
						RedirectAttributes redirectAttributes,
						Model model) {
		User user = userDetailsImpl.getUser();
		if(userService.isEmailChanged(userEditForm, user) && userService.isEmailRegistered(userEditForm.getEmail())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
			bindingResult.addError(fieldError);
		}
		
		if(bindingResult.hasErrors()) {
			model.addAttribute("userEditForm", userEditForm);
			
			return "/user/edit";
		}
		
		userService.updateUser(userEditForm, user);
		redirectAttributes.addFlashAttribute("successMessage", "会員情報を編集しました。");
	
		return "redirect:/user";
	}
}
