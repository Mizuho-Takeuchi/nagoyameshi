package com.example.nagoyameshi.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.form.CategoryEditForm;
import com.example.nagoyameshi.form.CategoryRegisterForm;
import com.example.nagoyameshi.service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {
	private final CategoryService categoryService;
	
	public AdminCategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}
	
	@GetMapping
	public String index(@RequestParam(name = "keyword" ,required = false) String keyword,
						@PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC) Pageable pageable, 
						Model model) {
		Page<Category> categoryPage;
		if(keyword != null && !keyword.isEmpty()) {
			categoryPage = categoryService.findCategoriesByNameLike(keyword, pageable);
		}else {
			categoryPage = categoryService.findAllCategories(pageable);
		}
		
		model.addAttribute("categoryPage", categoryPage);
		model.addAttribute("keyword", keyword);
		model.addAttribute("categoryRegisterForm", new CategoryRegisterForm());
		model.addAttribute("categoryEditForm", new CategoryEditForm());
		return "admin/categories/index";
	}
	
	@PostMapping("/create")
	public String create(@ModelAttribute @Validated CategoryRegisterForm categoryRegisterForm,
						BindingResult bindingResult,
						RedirectAttributes redirectAttributes) {
		if(bindingResult.hasErrors()) {
			redirectAttributes.addFlashAttribute("errorMessage", "カテゴリ名を入力してください。");
			return "redirect:/admin/categories";
		}
		
		categoryService.createCategory(categoryRegisterForm);
		redirectAttributes.addFlashAttribute("successMessage", "カテゴリを登録しました。");
		return "redirect:/admin/categories";
	}
	
	@PostMapping("/{id}/update")
	public String update(@ModelAttribute @Validated CategoryEditForm categoryEditForm,
						BindingResult bindingResult,
						RedirectAttributes redirectAttributes,
						@PathVariable(name ="id")Integer id) {
		Optional<Category> optionalCategory = categoryService.findCategoryById(id);
		
		if(optionalCategory.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "カテゴリが存在しません。");
			return "redirect:/admin/categories";
		}
		
		if(bindingResult.hasErrors()) {
			redirectAttributes.addFlashAttribute("errorMessage", "カテゴリ名を入力してください。");
			return "redirect:/admin/categories";
		}
		
		Category category = optionalCategory.get();
		categoryService.updateCategory(categoryEditForm, category);
		redirectAttributes.addFlashAttribute("successMessage", "カテゴリを編集しました。");
		return "redirect:/admin/categories";
	}
	
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable(name ="id")Integer id,
						RedirectAttributes redirectAttributes) {
		Optional<Category> optionalCategory = categoryService.findCategoryById(id);
		
		if(optionalCategory.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "カテゴリが存在しません。");
			return "redirect:/admin/categories";
		}
		
		Category category = optionalCategory.get();
		categoryService.deleteCategory(category);
		redirectAttributes.addFlashAttribute("successMessage", "カテゴリを削除しました。");
		return "redirect:/admin/categories";
	}
}
