package com.example.nagoyameshi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.ReservationService;

@Controller
public class ManagerHomeController {
	private final ReservationService reservationService;
	
	private final Logger log = LoggerFactory.getLogger(ManagerHomeController.class);
	
	public ManagerHomeController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}
	
	@GetMapping("/manager")
	public String index(Model model,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		Restaurant restaurant = null;
		if(userDetailsImpl != null) {
			restaurant = userDetailsImpl.getUser().getRestaurant();
		}
		long totalReservations = reservationService.countReservationByRestaurant(restaurant);
				
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("totalReservations", totalReservations);
		
		//ログ
		log.info("A restaurant manager accessed the toppage.");
		return "manager/index";
	}
}
