package com.example.nagoyameshi.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
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
	public String index(@PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC) Pageable pageable,
			@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			Model model) {
		Page<Reservation> reservationPage;
		LocalDateTime startDateTime;
		LocalDateTime endDateTime;

		//keywordと日付の両方が入力されている
		if (StringUtils.hasText(keyword) && startDate != null && endDate != null) {
			startDateTime = startDate.atStartOfDay();
			endDateTime = endDate.atTime(LocalTime.MAX);
			reservationPage = reservationService.findReservationsByRestaurantNameLikeAndReservedDatetimeBetween(keyword,
					startDateTime, endDateTime, pageable);
			//keywordのみ
		} else if (StringUtils.hasText(keyword)) {
			reservationPage = reservationService.findReservationsByRestaurantNameLike(keyword, pageable);
			//日付のみ
		} else if (startDate != null && endDate != null) {
			startDateTime = startDate.atStartOfDay();
			endDateTime = endDate.atTime(LocalTime.MAX);
			reservationPage = reservationService.findReservationsByReservedDatetimeBetween(startDateTime, endDateTime,
					pageable);
			//両方なし
		} else {
			reservationPage = reservationService.findAllReservations(pageable);
		}

		model.addAttribute("reservationPage", reservationPage);
		
		//CSV出力用に検索条件を渡す
		model.addAttribute("keyword", keyword);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		
		return "admin/reservations/index";
	}

	@GetMapping("/download")
	public void downloadCsv(@RequestParam(name = "keyword", required = false) String keyword,
							@RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
							@RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
							HttpServletResponse response) throws IOException {
		List<Reservation> reservationList;
		LocalDateTime startDateTime = null;
		LocalDateTime endDateTime = null;

		//keywordと日付の両方が入力されている
		if (StringUtils.hasText(keyword) && startDate != null && endDate != null) {
			startDateTime = startDate.atStartOfDay();
			endDateTime = endDate.atTime(LocalTime.MAX);
			reservationList = reservationService.findReservationsByRestaurantNameLikeAndReservedDatetimeBetween(keyword,
					startDateTime, endDateTime);
			//keywordのみ
		} else if (StringUtils.hasText(keyword)) {
			reservationList = reservationService.findReservationsByRestaurantNameLike(keyword);
			//日付のみ
		} else if (startDate != null && endDate != null) {
			startDateTime = startDate.atStartOfDay();
			endDateTime = endDate.atTime(LocalTime.MAX);
			reservationList = reservationService.findReservationsByReservedDatetimeBetween(startDateTime, endDateTime);
			//両方なし
		} else {
			reservationList = reservationService.findAllReservations();
		}

		//HTTPレスポンスの設定
		String fileName = "reservations_" + LocalDate.now() + ".csv"; //ファイル名
		response.setContentType("text/csv; charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

		//CSVデータの書き込み
		try (PrintWriter writer = response.getWriter()) {
			writer.write('\ufeff');

			// ヘッダー行の書き込み
			writer.println("予約ID,ユーザー,店舗名,予約日時,人数,予約作成日,最終編集日");

			// データ行の書き込み
			for (Reservation reservation : reservationList) {
				StringBuilder sb = new StringBuilder();
				//予約ID
				sb.append(reservation.getId()).append(",");
				//ユーザー
				if(reservation.getUser() != null) {
					sb.append("\"").append(reservation.getUser().getName().replace("\"", "\"\"")).append("\",");
				}else {
					sb.append("\"退会済みユーザー\",");
				}
				//店舗名
				if(reservation.getRestaurant() != null) {
					sb.append("\"").append(reservation.getRestaurant().getName().replace("\"", "\"\"")).append("\",");
				}else {
					sb.append("\"削除された店舗\",");
				}
				//予約日時
				sb.append(reservation.getReservedDatetime() != null ? reservation.getReservedDatetime() : "").append(",");
				//人数
				sb.append(reservation.getNumberOfPeople()).append(",");
				//予約作成日
				sb.append(reservation.getCreatedAt() != null ? reservation.getCreatedAt() : "");
				//最終編集日
				sb.append(reservation.getUpdatedAt() != null ? reservation.getUpdatedAt() : "");
				
				writer.println(sb.toString());
			}
			writer.flush();
		}
	}
}
