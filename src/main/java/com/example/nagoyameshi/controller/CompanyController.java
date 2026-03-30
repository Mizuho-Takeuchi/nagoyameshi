package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.entity.Company;
import com.example.nagoyameshi.service.CompanyService;

@Controller
public class CompanyController {
	private final CompanyService companyService;
	
	public CompanyController(CompanyService companyService) {
		this.companyService = companyService;
	}
	
	@GetMapping("/company")
	public String index(Model model) {
		Company company = companyService.findFirstCompanyByOrderByIdDesc();
		model.addAttribute("company",company);
		return "company/index";
	}
}
