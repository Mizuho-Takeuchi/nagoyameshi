package com.example.nagoyameshi.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.ReservationService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
public class ReservationController {
	private final ReservationService reservationService;
	private final RestaurantService restaurantService;
	private final Logger log = LoggerFactory.getLogger(ReservationController.class);
	
	public ReservationController(ReservationService reservationService,
								RestaurantService restaurantService) {
		this.reservationService = reservationService;
		this.restaurantService = restaurantService;
	}
	
	@GetMapping("/reservations")
	public String index(@PageableDefault(page = 0, size = 15, sort = "id" , direction = Direction.ASC)Pageable pageable,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						RedirectAttributes redirectAttributes,
						Model model) {
		User user = userDetailsImpl.getUser();
		String userRoleName = user.getRole().getName();
		if("ROLE_FREE_MEMBER".equals(userRoleName)) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}
		
		Page<Reservation> reservationPage = reservationService.findReservationsByUserOrderByReservedDatetimeDesc(pageable, user);
		LocalDateTime currentDateTime = LocalDateTime.now();
		
		model.addAttribute("reservationPage",reservationPage);
		model.addAttribute("currentDateTime",currentDateTime);
		return "reservations/index";
	}
	
	@GetMapping("/restaurants/{restaurantId}/reservations/register")
	public String register(@PathVariable("restaurantId")Integer restaurantId,
							@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
							RedirectAttributes redirectAttributes,
							Model model) {
		String userRoleName = userDetailsImpl.getUser().getRole().getName();
		if("ROLE_FREE_MEMBER".equals(userRoleName)) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}
		
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(restaurantId);
		if(optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage","店舗が存在しません。");
			return "redirect:/restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		List<Integer> restaurantRegularHolidays = restaurantService.findDayIndexesByRestaurantId(restaurantId);
		ReservationRegisterForm reservationRegisterForm = new ReservationRegisterForm();
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("restaurantRegularHolidays", restaurantRegularHolidays);
		model.addAttribute("reservationRegisterForm", reservationRegisterForm);
		return "reservations/register";
	}
	
	@PostMapping("/restaurants/{restaurantId}/reservations/create")
	public String create(@PathVariable("restaurantId")Integer restaurantId,
						@ModelAttribute @Validated ReservationRegisterForm reservationRegisterForm,			
						BindingResult bindingResult,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						Model model,
						RedirectAttributes redirectAttributes) {
		//ログ
		log.info("Starting the process: register reservation.");
		
		User user = userDetailsImpl.getUser();
		String userRoleName = user.getRole().getName();
		if("ROLE_FREE_MEMBER".equals(userRoleName)) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}
		
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(restaurantId);
		if(optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", 	"店舗が存在しません。");
			return "redirect:/restaurants";
		}
		
		LocalDate reservationDate = reservationRegisterForm.getReservationDate();
	    LocalTime reservationTime = reservationRegisterForm.getReservationTime();

	    if (reservationDate != null && reservationTime != null) {
	    	LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, reservationTime);
			if(!reservationService.isAtLeastTwoHoursInFuture(reservationDateTime)) {
				bindingResult.addError(new FieldError("reservationRegisterForm", "reservationTime", "当日の予約は2時間前までにお願いいたします。"));
			}
	    }
		
	    Restaurant restaurant = optionalRestaurant.get();
		if(bindingResult.hasErrors()) {
			List<Integer> restaurantRegularHolidays = restaurantService.findDayIndexesByRestaurantId(restaurantId);

           model.addAttribute("restaurant", restaurant);
           model.addAttribute("restaurantRegularHolidays", restaurantRegularHolidays);
           model.addAttribute("reservationRegisterForm", reservationRegisterForm);
           //ログ
           log.info("Faild to register reservation: Invalid reservation time.");
           return "reservations/register";
		}
		
		reservationService.createReservation(reservationRegisterForm, user, restaurant);
		redirectAttributes.addFlashAttribute("successMessage", "予約が完了しました。");
		//ログ
		log.info("Complete the process: register reservation.");
		return "redirect:/reservations";
	}
	
	@PostMapping("/reservations/{reservationId}/delete")
	public String delete(@PathVariable("reservationId")Integer reservationId,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		String userRoleName = user.getRole().getName();
		if("ROLE_FREE_MEMBER".equals(userRoleName)) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			return "redirect:/subscription/register";
		}
		
		Optional<Reservation> optionalReservation = reservationService.findReservationById(reservationId);
		if(optionalReservation.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "予約が存在しません。");
			return "redirect:/reservations";
		}
		
		Reservation reservation = optionalReservation.get();
		if(!reservation.getUser().getId().equals(user.getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");
			return "redirect:/reservations";
		}
		
		reservationService.deleteReservation(reservation);
		redirectAttributes.addFlashAttribute("successMessage", "予約をキャンセルしました。");
		return "redirect:/reservations";
	}
}