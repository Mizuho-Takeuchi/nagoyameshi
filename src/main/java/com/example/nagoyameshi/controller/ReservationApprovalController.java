package com.example.nagoyameshi.controller;

import java.time.LocalDateTime;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.ReservationService;

@Controller
@RequestMapping("/manager/approval")
public class ReservationApprovalController {
	private final ReservationService reservationService;
	
	public ReservationApprovalController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	@GetMapping
	public String index(@PageableDefault(page = 0, size = 15, sort = "id" , direction = Direction.ASC)Pageable pageable,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						Model model) {
		Restaurant restaurant = null;
		if(userDetailsImpl != null) {
			restaurant = userDetailsImpl.getUser().getRestaurant();
		}
		
		Page<Reservation> reservationPage = reservationService.findReservationsByRestaurantAndStatus(restaurant, 0, pageable);
		LocalDateTime currentDateTime = LocalDateTime.now();
		
		model.addAttribute("reservationPage", reservationPage);
		model.addAttribute("currentDateTime", currentDateTime);
		
		return "manager/approval/index";
	}
	
	@PostMapping("/{id}")
	public String approve(@PathVariable(name = "id") Integer id,
							RedirectAttributes redirectAttributes) {
		reservationService.approve(id);
		redirectAttributes.addFlashAttribute("successMessage", "予約を承認しました。");
		return "redirect:/manager/approval/";
	}
}
