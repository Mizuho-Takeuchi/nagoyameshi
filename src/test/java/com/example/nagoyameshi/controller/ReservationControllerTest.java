package com.example.nagoyameshi.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.service.ReservationService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReservationControllerTest {
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ReservationService reservationService;
	
	//予約一覧ページ
	@Test
	public void 未ログインの場合は予約一覧ページからログインページにリダイレクトする() throws Exception{
		mockMvc.perform(get("/reservations"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 無料会員としてログイン済みの場合は予約一覧ページから有料プラン登録ページにリダイレクトする() throws Exception{
		mockMvc.perform(get("/reservations"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/subscription/register"));
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	public void 有料会員としてログイン済みの場合は予約一覧ページが正しく表示される() throws Exception{
		mockMvc.perform(get("/reservations"))
				.andExpect(status().isOk())
				.andExpect(view().name("reservations/index"));
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合は予約一覧ページが表示されずに403エラーが発生する() throws Exception{
		mockMvc.perform(get("/reservations"))
				.andExpect(status().isForbidden());
	}
	
	//予約ページ
	@Test
	public void 未ログインの場合は予約ページからログインページにリダイレクトする() throws Exception{
		mockMvc.perform(get("/restaurants/1/reservations/register"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 無料会員としてログイン済みの場合は予約ページから有料プラン登録ページにリダイレクトする() throws Exception{
		mockMvc.perform(get("/restaurants/1/reservations/register"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/subscription/register"));
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	public void 有料会員としてログイン済みの場合は予約ページが正しく表示される() throws Exception{
		mockMvc.perform(get("/restaurants/1/reservations/register"))
				.andExpect(status().isOk())
				.andExpect(view().name("reservations/register"));
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合は予約ページが表示されずに403エラーが発生する() throws Exception{
		mockMvc.perform(get("/restaurants/1/reservations/register"))
				.andExpect(status().isForbidden());
	}
	
	//予約機能
	@Transactional
	public void 未ログインの場合は予約せずにログインページにリダイレクトする() throws Exception{
		long countBefore = reservationService.countReservations();
		
		mockMvc.perform(post("/restaurants/1/reservations/create").with(csrf())
				.param("reservationDate", "2030-12-12")
				.param("reservationTime", "12:00:00")
				.param("numberOfPeople", "50"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login"));
		
		long countAfter = reservationService.countReservations();
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Transactional
	public void 無料会員としてログイン済みの場合は予約せずに有料プラン登録ページにリダイレクトする() throws Exception{
		long countBefore = reservationService.countReservations();
		
		mockMvc.perform(post("/restaurants/1/reservations/create").with(csrf())
				.param("reservationDate", "2030-12-12")
				.param("reservationTime", "12:00:00")
				.param("numberOfPeople", "50"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/subscription/register"));
		
		long countAfter = reservationService.countReservations();
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	@Transactional
	public void 有料会員としてログイン済みの場合は予約後に予約一覧ページにリダイレクトする() throws Exception{
		long countBefore = reservationService.countReservations();
		
		mockMvc.perform(post("/restaurants/1/reservations/create").with(csrf())
				.param("reservationDate", "2030-12-12")
				.param("reservationTime", "12:00:00")
				.param("numberOfPeople", "50"))
			.andExpect(status().is3xxRedirection())	
			.andExpect(redirectedUrl("/reservations"));
		
		long countAfter = reservationService.countReservations();
		assertThat(countAfter).isEqualTo(countBefore + 1);
		Reservation reservation = reservationService.findFirstReservationByOrderByIdDesc();
		assertThat(reservation.getReservedDatetime()).isEqualTo(LocalDateTime.parse("2030-12-12 12:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		assertThat(reservation.getNumberOfPeople()).isEqualTo(50);
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	@Transactional
	public void 管理者としてログイン済みの場合は予約せずに403エラーが発生する() throws Exception{
		long countBefore = reservationService.countReservations();
		
		mockMvc.perform(post("/restaurants/1/reservations/create").with(csrf())
				.param("reservationDate", "2030-12-12")
				.param("reservationTime", "12:00:00")
				.param("numberOfPeople", "50"))
			.andExpect(status().isForbidden());
		
		long countAfter = reservationService.countReservations();
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	//予約キャンセル機能
	@Test
	@Transactional
	public void 未ログインの場合は予約をキャンセルせずにログインページにリダイレクトする() throws Exception{
		mockMvc.perform(post("/reservations/1/delete").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
		Optional<Reservation> optionalReservation = reservationService.findReservationById(1);
		assertThat(optionalReservation).isPresent();
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Transactional
	public void 無料会員としてログイン済みの場合は予約をキャンセルせずに有料プラン登録ページにリダイレクトする() throws Exception{
		mockMvc.perform(post("/reservations/1/delete").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/subscription/register"));
		Optional<Reservation> optionalReservation = reservationService.findReservationById(1);
		assertThat(optionalReservation).isPresent();
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	@Transactional
	public void 有料会員としてログイン済みの場合は自身の予約キャンセル後に予約一覧ページにリダイレクトする() throws Exception{
		mockMvc.perform(post("/reservations/1/delete").with(csrf()))
				.andExpect(status().is3xxRedirection())	
				.andExpect(redirectedUrl("/reservations"));
		Optional<Reservation> optionalReservation = reservationService.findReservationById(1);
		assertThat(optionalReservation).isEmpty();
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	@Transactional
	public void 有料会員としてログイン済みの場合は他人の予約をキャンセルせずに予約一覧ページにリダイレクトする() throws Exception{
		mockMvc.perform(post("/reservations/21/delete").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/reservations"));
		Optional<Reservation> optionalReservation = reservationService.findReservationById(1);
		assertThat(optionalReservation).isPresent();
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	@Transactional
	public void 管理者としてログイン済みの場合は予約をキャンセルせずに403エラーが発生する() throws Exception{
		mockMvc.perform(post("/reservations/1/delete").with(csrf()))
				.andExpect(status().isForbidden());
		Optional<Reservation> optionalReservation = reservationService.findReservationById(1);
		assertThat(optionalReservation).isPresent();
	}
}
