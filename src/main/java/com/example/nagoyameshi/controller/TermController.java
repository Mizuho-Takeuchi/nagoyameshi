package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.entity.Term;
import com.example.nagoyameshi.service.TermService;

@Controller
public class TermController {
	private final TermService termService;
	
	public TermController(TermService termService) {
		this.termService = termService;
	}
	@GetMapping("/terms")
	public String index(Model model) {
		Term term = termService.findFirstTermByOrderByIdDesc();
		model.addAttribute("term", term);
		return "terms/index";
	}
}
