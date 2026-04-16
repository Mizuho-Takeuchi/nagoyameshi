package com.example.nagoyameshi.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.service.UserService;

@Controller
@RequestMapping("/admin/admin")
public class AdminAdminController {
	private final UserService userService;
	
	public AdminAdminController(UserService userService) {
		this.userService = userService;
	}
	
	@GetMapping
	public String index(@PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC) Pageable pageable,
 						Model model) {
		Page<User> userPage = userService.findUserByRole_Name("ROLE_ADMIN",pageable);
		
		model.addAttribute("userPage", userPage);
		return "admin/admin/index";
	}
}
