package com.example.nagoyameshi.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.service.UserService;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
	private final UserService userService;
	
	public AdminUserController(UserService userService) {
		this.userService = userService;
	}
	
	@GetMapping
	public String index(@PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC) Pageable pageable,
			 			@RequestParam (name= "keyword", required = false)String keyword,
			 			Model model) {
		Page<User> userPage;
		if(keyword != null && !keyword.isEmpty()) {
			userPage = userService.findUsersByNameLikeOrFuriganaLike(keyword, keyword, pageable);
		}else {
			userPage = userService.findAllUsers(pageable);
		}
		
		model.addAttribute("userPage", userPage);
		model.addAttribute("keyword", keyword);
		return "admin/users/index";
	}
	
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id,
						Model model,
						RedirectAttributes redirectAttributes) {
		Optional<User> optionalUser = userService.findUserById(id);
		
		if(optionalUser.isPresent()) {
			User user = optionalUser.get();
			model.addAttribute("user", user);
			return "admin/users/show";
		}else {
			redirectAttributes.addFlashAttribute("errorMessage", "ユーザーが存在しません。");
			return "redirect:/admin/users";
		}
	}
}
