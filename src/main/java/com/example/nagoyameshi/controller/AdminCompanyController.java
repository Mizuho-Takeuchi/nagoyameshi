package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Company;
import com.example.nagoyameshi.form.CompanyEditForm;
import com.example.nagoyameshi.service.CompanyService;

@Controller
@RequestMapping("/admin/company")
public class AdminCompanyController {
	private final CompanyService companyService;
	
	public AdminCompanyController(CompanyService companyService) {
		this.companyService = companyService;
	}
	
	@GetMapping
	public String index(Model model) {
		Company company = companyService.findFirstCompanyByOrderByIdDesc();
		model.addAttribute("company", company);
		return "admin/company/index";
	}
	
	@GetMapping("/edit")
	public String edit(Model model) {
		Company company = companyService.findFirstCompanyByOrderByIdDesc();
		CompanyEditForm companyEditForm =new CompanyEditForm(company.getName(),
															company.getPostalCode(),
															company.getAddress(),
															company.getRepresentative(),
															company.getEstablishmentDate(),
															company.getCapital(),
															company.getBusiness(),
															company.getNumberOfEmployees());
		model.addAttribute("companyEditForm", companyEditForm);
		
		return "admin/company/edit";
	}
	
	@PostMapping("/update")
	public String update(@ModelAttribute @Validated CompanyEditForm companyEditForm,
						BindingResult bindingResult,
						Model model,
						RedirectAttributes redirectAttributes) {
		if(bindingResult.hasErrors()) {
			model.addAttribute("companyEditForm", companyEditForm);
			return "redirect:/admin/company/edit";
		}
		
		Company company = companyService.findFirstCompanyByOrderByIdDesc();
		companyService.updateCompany(company, companyEditForm);
		redirectAttributes.addFlashAttribute("successMessage", "会社概要を編集しました。");
		return "redirect:/admin/company";
	}
}
