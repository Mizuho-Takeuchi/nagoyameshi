package com.example.nagoyameshi.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private final Logger log = LoggerFactory.getLogger(ReservationService.class);
	
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
		reservation.setStatus(0);
		
		reservationRepository.save(reservation);
	}
	
	@Transactional
	public void deleteReservation(Reservation reservation) {
		reservationRepository.delete(reservation);
		log.info("Complete the process: delete reservation.");
	}
	
	@Transactional
	public boolean isAtLeastTwoHoursInFuture(LocalDateTime reservationDataTime) {
		//現在の日付を取得
		LocalDateTime now = LocalDateTime.now();
		
		return Duration.between(now,reservationDataTime).toHours() >= 2;
	}
	
	//管理画面一覧表示用
	public Page<Reservation> findReservationsByRestaurantNameLike(String keyword,Pageable pageable){
		return reservationRepository.findByRestaurantNameLikeOrderByReservedDatetimeDesc("%"+keyword+"%", pageable);
	}
	
	public Page<Reservation> findAllReservations(Pageable pageable){
		return reservationRepository.findAllByOrderByReservedDatetimeDesc(pageable);
	}
	
	public Page<Reservation> findReservationsByRestaurantNameLikeAndReservedDatetimeBetween(String keyword, 
																						    LocalDateTime start, 
																						    LocalDateTime end, 
																						    Pageable pageable){
		return reservationRepository.findByRestaurantNameLikeAndReservedDatetimeBetweenOrderByReservedDatetimeDesc("%"+keyword+"%", start, end, pageable);
	}
	
	public Page<Reservation> findReservationsByReservedDatetimeBetween(LocalDateTime start, 
																	    LocalDateTime end, 
																	    Pageable pageable){
		return reservationRepository.findByReservedDatetimeBetweenOrderByReservedDatetimeDesc(start, end, pageable);
	}
	
	//CSV出力用
	public List<Reservation> findReservationsByRestaurantNameLike(String keyword){
		return reservationRepository.findByRestaurantNameLikeOrderByReservedDatetimeDesc("%"+keyword+"%");
	}
	
	public List<Reservation> findAllReservations(){
		return reservationRepository.findAllByOrderByReservedDatetimeDesc();
	}
	
	public List<Reservation> findReservationsByRestaurantNameLikeAndReservedDatetimeBetween(String keyword, 
																						    	LocalDateTime start, 
																						    	LocalDateTime end){
		return reservationRepository.findByRestaurantNameLikeAndReservedDatetimeBetweenOrderByReservedDatetimeDesc("%"+keyword+"%", start, end);
	}
	
	public List<Reservation> findReservationsByReservedDatetimeBetween(LocalDateTime start, 
																	    	LocalDateTime end){
		return reservationRepository.findByReservedDatetimeBetweenOrderByReservedDatetimeDesc(start, end);
	}
	
	//店舗管理者ページ用
	public long countReservationByRestaurantAndStatus(Restaurant restaurant, Integer status) {
		return reservationRepository.countByRestaurantAndStatus(restaurant, status);
	}
	
	public Page<Reservation> findReservationsByRestaurantAndStatus(Restaurant restaurant, Integer status, Pageable pageable){
		return reservationRepository.findByRestaurantAndStatusOrderByReservedDatetimeDesc(restaurant, status, pageable);
	}
	
	public long countReservationsByRestaurantAndReservedDatetimeBetweenAndStatus(Restaurant restaurant, LocalDateTime start, LocalDateTime end, Integer status) {
		return reservationRepository.countByRestaurantAndReservedDatetimeBetweenAndStatus(restaurant, start, end, status);
	}
	
	public Page<Reservation> findReservationsByRestaurantAndReservedDatetimeBetweenAndStatus(Restaurant restaurant, LocalDateTime start, LocalDateTime end, Integer status, Pageable pageable){
		return reservationRepository.findByRestaurantAndReservedDatetimeBetweenAndStatusOrderByReservedDatetimeDesc(restaurant, start, end, status, pageable);
	}
	
	public void approve(Integer id) {
		Optional<Reservation> optionalReservation = reservationRepository.findById(id);
		Reservation reservation = null;
		if(optionalReservation.isPresent()) {
			reservation = optionalReservation.get();
		}
		reservation.setStatus(1);
		reservationRepository.save(reservation);
	}
}