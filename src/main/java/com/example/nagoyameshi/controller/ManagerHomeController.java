package com.example.nagoyameshi.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
		
		LocalDate today = LocalDate.now();
		LocalDateTime startOfToday = today.atStartOfDay();
		LocalDateTime endOfToday = today.atTime(23, 59, 59);
		LocalDate tomorrow = today.plusDays(1);
		LocalDateTime startOfTomorrow = tomorrow.atStartOfDay();
		LocalDateTime endOfTomorrow = tomorrow.atTime(23, 59, 59);
		long todaysReservations = reservationService.countReservationsByRestaurantAndReservedDatetimeBetweenAndStatus(restaurant, startOfToday, endOfToday, 1);
		long tomorrowsReservations = reservationService.countReservationsByRestaurantAndReservedDatetimeBetweenAndStatus(restaurant, startOfTomorrow, endOfTomorrow, 1);
		long approvalReservations = reservationService.countReservationByRestaurantAndStatus(restaurant, 0);
				
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("todaysReservations", todaysReservations);
		model.addAttribute("tomorrowsReservations", tomorrowsReservations);
		model.addAttribute("approvalReservations", approvalReservations);
		
		//ログ
		log.info("A restaurant manager accessed the toppage.");
		return "manager/index";
	}
}
