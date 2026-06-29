package com.example.nagoyameshi.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						@RequestParam(name = "date", required = false) String date) {
		Restaurant restaurant = null;
		if(userDetailsImpl != null) {
			restaurant = userDetailsImpl.getUser().getRestaurant();
		}
		
		Page<Reservation> reservationPage = null;
		LocalDate today = LocalDate.now();
		if(date == null || date.isEmpty()) {
			reservationPage = reservationService.findReservationsByRestaurantAndStatus(restaurant, 1, pageable);
		}else {
			if(date.equals("today")) {
				LocalDateTime startOfToday = today.atStartOfDay();
				LocalDateTime endOfToday = today.atTime(23, 59, 59);
				reservationPage = reservationService.findReservationsByRestaurantAndReservedDatetimeBetweenAndStatus(restaurant, startOfToday, endOfToday, 1, pageable);
			}else if(date.equals("tomorrow")) {
				LocalDate tomorrow = today.plusDays(1);
				LocalDateTime startOfTomorrow = tomorrow.atStartOfDay();
				LocalDateTime endOfTomorrow = tomorrow.atTime(23, 59, 59);
				reservationPage = reservationService.findReservationsByRestaurantAndReservedDatetimeBetweenAndStatus(restaurant, startOfTomorrow, endOfTomorrow, 1, pageable);
			}else {
				reservationPage = reservationService.findReservationsByRestaurantAndStatus(restaurant, 1, pageable);
			}
		}
		
		model.addAttribute("reservationPage", reservationPage);
		
		return "manager/reservations/index";
	}

}
