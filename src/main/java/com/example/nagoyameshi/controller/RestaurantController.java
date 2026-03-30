package com.example.nagoyameshi.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.FavoriteService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
@RequestMapping("/restaurants")
public class RestaurantController {
	private final RestaurantService restaurantService;
	private final CategoryService categoryService;
	private final FavoriteService favoriteService;
	
	public RestaurantController(RestaurantService restaurantService,
								CategoryService categoryService,
								FavoriteService favoriteService) {
		this.restaurantService = restaurantService;
		this.categoryService = categoryService;
		this.favoriteService = favoriteService;
	}
	
	@GetMapping
	public String index(@PageableDefault (page = 0, size = 15, sort = "id", direction = Direction.ASC)Pageable pageable,
						@RequestParam(name = "keyword", required = false)String keyword,
						@RequestParam(name = "categoryId", required = false)Integer categoryId,
						@RequestParam(name = "price", required = false)Integer price,
						@RequestParam(name = "order", required = false)String order,
						Model model) {
		Page<Restaurant> restaurantPage = null;
		
		if(keyword != null && !keyword.isEmpty()) {
			if(order != null && order.equals("lowestPriceAsc")) {
				restaurantPage = restaurantService.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(pageable, keyword, keyword, keyword);
			}else if(order != null && order.equals("ratingDesc")){
				restaurantPage = restaurantService.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(pageable, keyword, keyword, keyword);
			}else if(order != null && order.equals("popularDesc")){
				restaurantPage = restaurantService.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByReservationCountDesc(pageable, keyword, keyword, keyword);
			}else {
				restaurantPage = restaurantService.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(pageable, keyword, keyword, keyword);
			}
		}else if(categoryId != null) {
			if(order != null && order.equals("lowestPriceAsc")) {
				restaurantPage = restaurantService.findRestaurantsByCategoryIdOrderByCreatedAtDesc(pageable, categoryId);
			}else if(order != null && order.equals("ratingDesc")){
				restaurantPage = restaurantService.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(pageable, keyword, keyword, keyword);
			}else if(order != null && order.equals("popularDesc")){
				restaurantPage = restaurantService.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByReservationCountDesc(pageable, keyword, keyword, keyword);
			}else {
				restaurantPage = restaurantService.findRestaurantsByCategoryIdOrderByLowestPriceAsc(pageable, categoryId);
			}
		}else if(price != null) {
			if(order != null && order.equals("lowestPriceAsc")) {
				restaurantPage = restaurantService.findRestaurantsByLowestPriceLessThanEqualOrderByCreatedAtDesc(pageable, price);
			}else if(order != null && order.equals("ratingDesc")){
				restaurantPage = restaurantService.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(pageable, keyword, keyword, keyword);
			}else if(order != null && order.equals("popularDesc")){
				restaurantPage = restaurantService.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByReservationCountDesc(pageable, keyword, keyword, keyword);
			}else {
				restaurantPage = restaurantService.findRestaurantsByLowestPriceLessThanEqualOrderByLowestPriceAsc(pageable, price);
			}
		}else {
			if(order != null && order.equals("lowestPriceAsc")) {
				restaurantPage = restaurantService.findAllRestaurantsByOrderByCreatedAtDesc(pageable);
			}else if(order != null && order.equals("ratingDesc")){
				restaurantPage = restaurantService.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(pageable, keyword, keyword, keyword);
			}else if(order != null && order.equals("popularDesc")){
				restaurantPage = restaurantService.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByReservationCountDesc(pageable, keyword, keyword, keyword);
			}else {
				restaurantPage = restaurantService.findAllRestaurantsByOrderByLowestPriceAsc(pageable);
			}
		}
		
		List<Category> categories = categoryService.findAllCategories();
		model.addAttribute("restaurantPage", restaurantPage);
	    model.addAttribute("categories", categories);
	    model.addAttribute("keyword", keyword);
	    model.addAttribute("categoryId", categoryId);
	    model.addAttribute("price", price);
	    model.addAttribute("order", order);
		return "restaurants/index";
	}
	
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id")Integer id, 
						Model model,
						RedirectAttributes redirectAttributes,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);
		
		if(optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			return "redirect:/restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		
		Favorite favorite = null;
		boolean isFavorite = false;

		if (userDetailsImpl != null) {
		    User user = userDetailsImpl.getUser();
		    isFavorite = favoriteService.isFavorite(restaurant, user);
		    if (isFavorite) {
		        favorite = favoriteService.findFavoriteByRestaurantAndUser(restaurant, user);
		    }
		}
		
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("favorite", favorite);
		model.addAttribute("isFavorite", isFavorite);
		return "restaurants/show";
	}
}
