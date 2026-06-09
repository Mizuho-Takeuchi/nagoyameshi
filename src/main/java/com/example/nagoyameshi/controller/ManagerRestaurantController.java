package com.example.nagoyameshi.controller;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.form.RestaurantEditForm;
import com.example.nagoyameshi.form.RestaurantRegisterForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.CategoryRestaurantService;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.RegularHolidayRestaurantService;
import com.example.nagoyameshi.service.RegularHolidayService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
@RequestMapping("manager/restaurants")
public class ManagerRestaurantController {
	private final RestaurantService restaurantService;
	private final CategoryService categoryService;
	private final RegularHolidayService regularHolidayService;
	private final CategoryRestaurantService categoryRestaurantService;
	private final RegularHolidayRestaurantService regularHolidayRestaurantService;
	
	public ManagerRestaurantController(RestaurantService restaurantService,
										CategoryService categoryService,
										RegularHolidayService regularHolidayService,
										CategoryRestaurantService categoryRestaurantService,
										RegularHolidayRestaurantService regularHolidayRestaurantService) {
		this.restaurantService = restaurantService;
		this.categoryService = categoryService;
		this.regularHolidayService = regularHolidayService;
		this.categoryRestaurantService = categoryRestaurantService;
		this.regularHolidayRestaurantService = regularHolidayRestaurantService; 
	}
	
	@GetMapping
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						Model model) {
		Restaurant restaurant = null;
		if(userDetailsImpl != null) {
			restaurant = userDetailsImpl.getUser().getRestaurant();
		}
		
		model.addAttribute("restaurant", restaurant);
		
		return "manager/restaurants/index";
	}
	
	@GetMapping("/register")
	public String register(Model model) {
		model.addAttribute("restaurantRegisterForm", new RestaurantRegisterForm());
		model.addAttribute("categories", categoryService.findAllCategories());
		model.addAttribute("regularHolidays", regularHolidayService.findAllRegularHolidays());
		return "manager/restaurants/register";
	}
	
	@PostMapping("/create")
	public String create(@ModelAttribute @Validated RestaurantRegisterForm restaurantRegisterForm,
						BindingResult bindingResult,
						RedirectAttributes redirectAttributes,
						Model model) {
		Integer lowestPrice = restaurantRegisterForm.getLowestPrice();
        Integer highestPrice = restaurantRegisterForm.getHighestPrice();
        LocalTime openingTime = restaurantRegisterForm.getOpeningTime();
        LocalTime closingTime = restaurantRegisterForm.getClosingTime();

        if (lowestPrice != null && highestPrice != null && !restaurantService.isValidPrices(lowestPrice, highestPrice)) {
               FieldError lowestPriceError = new FieldError(bindingResult.getObjectName(), "lowestPrice", "最低価格は最高価格以下に設定してください。");
               FieldError highestPriceError = new FieldError(bindingResult.getObjectName(), "highestPrice", "最高価格は最低価格以上に設定してください。");
               bindingResult.addError(lowestPriceError);
               bindingResult.addError(highestPriceError);
        }

        if (openingTime != null && closingTime != null && !restaurantService.isValidBusinessHours(openingTime, closingTime)) {
               FieldError openingTimeError = new FieldError(bindingResult.getObjectName(), "openingTime", "開店時間は閉店時間よりも前に設定してください。");
               FieldError closingTimeError = new FieldError(bindingResult.getObjectName(), "closingTime", "閉店時間は開店時間よりも後に設定してください。");
               bindingResult.addError(openingTimeError);
               bindingResult.addError(closingTimeError);
        }

        if (bindingResult.hasErrors()) {
           model.addAttribute("restaurantRegisterForm", restaurantRegisterForm);
           model.addAttribute("categories", categoryService.findAllCategories());
           model.addAttribute("regularHolidays", regularHolidayService.findAllRegularHolidays());
           return "manager/restaurants/register";
	    }
        
        restaurantService.createRestaurant(restaurantRegisterForm);
        redirectAttributes.addFlashAttribute("successMessage", "店舗を登録しました。");

        return "redirect:/manager/restaurants";
	}
	
	@GetMapping("/{id}/edit")
	public String edit(@PathVariable(name = "id") Integer id,
						RedirectAttributes redirectAttributes,
						Model model) {
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);
		
		if(optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			return "redirect:/manager/restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		List<Integer> categoryIds = categoryRestaurantService.findCategoryIdsByRestaurantOrderByIdAsc(restaurant);
		List<Integer> currentRegularHolidays = regularHolidayRestaurantService.findRegularHolidayIdsByRestaurant(restaurant);
		
		RestaurantEditForm restaurantEditForm = new RestaurantEditForm(restaurant.getName(),
														                null,
														                restaurant.getDescription(),
														                restaurant.getLowestPrice(),
														                restaurant.getHighestPrice(),
														                restaurant.getPostalCode(),
														                restaurant.getAddress(),
														                restaurant.getMapUrl(),												                restaurant.getOpeningTime(),
														                restaurant.getClosingTime(),
														                restaurant.getSeatingCapacity(),
														                categoryIds,
														                currentRegularHolidays);
																		//↑仮
		
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("restaurantEditForm", restaurantEditForm);
		model.addAttribute("categories", categoryService.findAllCategories());
		model.addAttribute("regularHolidays", regularHolidayService.findAllRegularHolidays());
		return "manager/restaurants/edit";
	}
	
	@PostMapping("/{id}/update")
	public String update(@ModelAttribute @Validated RestaurantEditForm restaurantEditForm,
						BindingResult bindingResult,
						@PathVariable(name = "id") Integer id,
						RedirectAttributes redirectAttributes,
						Model model) {
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);
        if(optionalRestaurant.isEmpty()) {
        	redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
        	return "redirect:/manager/restaurant";
        }
        Restaurant restaurant = optionalRestaurant.get();
		
		Integer lowestPrice = restaurantEditForm.getLowestPrice();
        Integer highestPrice = restaurantEditForm.getHighestPrice();
        LocalTime openingTime = restaurantEditForm.getOpeningTime();
        LocalTime closingTime = restaurantEditForm.getClosingTime();

        if (lowestPrice != null && highestPrice != null && !restaurantService.isValidPrices(lowestPrice, highestPrice)) {
               FieldError lowestPriceError = new FieldError(bindingResult.getObjectName(), "lowestPrice", "最低価格は最高価格以下に設定してください。");
               FieldError highestPriceError = new FieldError(bindingResult.getObjectName(), "highestPrice", "最高価格は最低価格以上に設定してください。");
               bindingResult.addError(lowestPriceError);
               bindingResult.addError(highestPriceError);
        }

        if (openingTime != null && closingTime != null && !restaurantService.isValidBusinessHours(openingTime, closingTime)) {
               FieldError openingTimeError = new FieldError(bindingResult.getObjectName(), "openingTime", "開店時間は閉店時間よりも前に設定してください。");
               FieldError closingTimeError = new FieldError(bindingResult.getObjectName(), "closingTime", "閉店時間は開店時間よりも後に設定してください。");
               bindingResult.addError(openingTimeError);
               bindingResult.addError(closingTimeError);
        }

        if (bindingResult.hasErrors()) {
           model.addAttribute("restaurant", restaurant);
           model.addAttribute("restaurantRegisterForm", restaurantEditForm);
           model.addAttribute("categories", categoryService.findAllCategories());
           model.addAttribute("regularHolidays", regularHolidayService.findAllRegularHolidays());
           return "manager/restaurants/edit";
	    }
        
        restaurantService.updateRestaurant(restaurantEditForm, restaurant);
        redirectAttributes.addFlashAttribute("successMessage", "店舗を編集しました。");

        return "redirect:/manager/restaurants";
	}
	
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable(name = "id") Integer id,
						RedirectAttributes redirectAttributes) {
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);
        if(optionalRestaurant.isEmpty()) {
        	redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
        	return "redirect:/manager/restaurant";
        }
        Restaurant restaurant = optionalRestaurant.get();
        
        restaurantService.deleteRestaurant(restaurant);
        redirectAttributes.addFlashAttribute("successMessage", "店舗を削除しました。");
        return "redirect:/manager/restaurants";
	}
}
