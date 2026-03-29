package com.example.nagoyameshi.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.example.nagoyameshi.repository.ReservationRepository;

@Service
public class ReservationService {
	private final ReservationRepository reservationRepository;
	
	public ReservationService(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}
	
	public Optional<Reservation> findReservationById(Integer id) {
		return reservationRepository.findById(id);
	}
	
	public Page<Reservation> findReservationsByUserOrderByReservedDatetimeDesc(Pageable pageable, User user){
		return reservationRepository.findByUserOrderByReservedDatetimeDesc(pageable, user);
	}
	
	public long countReservations() {
		return reservationRepository.count();
	}
	
	public Reservation findFirstReservationByOrderByIdDesc() {
		return reservationRepository.findFirstByOrderByIdDesc();
	}
	
	@Transactional
	public void createReservation(ReservationRegisterForm reservationRegisterForm,
									User user,
									Restaurant restaurant) {
		Reservation reservation = new Reservation();
		LocalDateTime dateTime = LocalDateTime.of(reservationRegisterForm.getReservationDate(), reservationRegisterForm.getReservationTime());
		reservation.setReservedDatetime(dateTime);
		reservation.setNumberOfPeople(reservationRegisterForm.getNumberOfPeople());
		reservation.setRestaurant(restaurant);
		reservation.setUser(user);
		
		reservationRepository.save(reservation);
	}
	
	@Transactional
	public void deleteReservation(Reservation reservation) {
		reservationRepository.delete(reservation);
	}
	
	@Transactional
	public boolean isAtLeastTwoHoursInFuture(LocalDateTime reservationDataTime) {
		//現在の日付を取得
		LocalDateTime now = LocalDateTime.now();
		
		return Duration.between(now,reservationDataTime).toHours() >= 2;
	}
}