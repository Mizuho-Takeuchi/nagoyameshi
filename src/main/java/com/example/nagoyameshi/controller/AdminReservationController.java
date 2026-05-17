package com.example.nagoyameshi.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.service.ReservationService;

@Controller
@RequestMapping("/admin/reservations")
public class AdminReservationController {
	private final ReservationService reservationService;
	
	public AdminReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}
	
	@GetMapping
	public String index(@PageableDefault (page =0, size = 15, sort ="id", direction = Direction.ASC)Pageable pageable, 
						@RequestParam(name ="keyword", required = false) String keyword,
						Model model) {
		Page<Reservation> reservationPage;
		if(keyword != null && keyword.isEmpty()) {
			reservationPage = reservationService.findReservationsByRestaurantNameLike(keyword, pageable);
		}else {
			reservationPage = reservationService.findAllReservationsByOrderByCreatedAtDesc(pageable);
		}
		
		model.addAttribute("reservationPage", reservationPage);
		return "admin/reservations/index";
	}
	
	
}
