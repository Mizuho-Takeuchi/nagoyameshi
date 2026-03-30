package com.example.nagoyameshi.controller;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.FavoriteService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
public class FavoriteController {
	private final FavoriteService favoriteService;
	private final RestaurantService restaurantService;
	
	public FavoriteController(FavoriteService favoriteService,
							RestaurantService restaurantService) {
		this.favoriteService = favoriteService;
		this.restaurantService = restaurantService;
	}
	
	@GetMapping("/favorites")
	public String index(@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC)Pageable pageable,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						RedirectAttributes redirectAttributes,
						Model model) {
		User user = userDetailsImpl.getUser();
		String userRoleName = user.getRole().getName();
		if("ROLE_FREE_MEMBER".equals(userRoleName)) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}
		Page<Favorite> favoritePage = favoriteService.findFavoritesByUserOrderByCreatedAtDesc(pageable, user);
		model.addAttribute("favoritePage", favoritePage);
		return "favorites/index";
	}
	
	@PostMapping("/restaurants/{restaurantId}/favorites/create")
	public String create(@PathVariable("restaurantId")Integer restaurantId,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		String userRoleName = user.getRole().getName();
		if("ROLE_FREE_MEMBER".equals(userRoleName)) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}
		
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(restaurantId);
		if(optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			return "redirect:/restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		favoriteService.createFavorite(restaurant, user);
		
		redirectAttributes.addFlashAttribute("successMessage", "お気に入りに追加しました。");
		return "redirect:/restaurants/{restaurantId}";
	}
	
	@PostMapping("/favorites/{favoriteId}/delete")
	public String delete(@PathVariable("favoriteId")Integer favoriteId,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						RedirectAttributes redirectAttributes,
						HttpServletRequest httpServletRequest) {
		User user = userDetailsImpl.getUser();
		String userRoleName = user.getRole().getName();
		if("ROLE_FREE_MEMBER".equals(userRoleName)) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}
		
		//元のページにリダイレクトさせる
		String referer = httpServletRequest.getHeader("Referer");
		
		Optional<Favorite> optionalFavorite = favoriteService.findFavoriteById(favoriteId);
		if(optionalFavorite.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "お気に入りが存在しません。");
			return "redirect:" + (referer != null ? referer : "/favorites");
		}
		
		Favorite favorite = optionalFavorite.get();
		if(!user.getId().equals(favorite.getUser().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");
			return "redirect:" + (referer != null ? referer : "/favorites");
		}
		
		favoriteService.deleteFavorite(favorite);
		redirectAttributes.addFlashAttribute("successMessage", "お気に入りを解除しました。");
		return "redirect:" + (referer != null ? referer : "/favorites");
	}
}
