package com.example.nagoyameshi.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.ReservationService;

@Controller
@RequestMapping("/manager/reservations")
public class ManagerReservationController {
	private final ReservationService reservationService;
	
	public ManagerReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}
	
	@GetMapping
	public String index(@PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC)Pageable pageable,
						Model model,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		Restaurant restaurant = null;
		if(userDetailsImpl != null) {
			restaurant = userDetailsImpl.getUser().getRestaurant();
		}
		
		Page<Reservation> reservationPage = reservationService.findReservationsByRestaurant(restaurant, pageable);
		model.addAttribute("reservationPage", reservationPage);
		
		return "manager/reservations/index";
	}
}
