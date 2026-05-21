package com.example.nagoyameshi.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.User;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
   public Page<Reservation> findByUserOrderByReservedDatetimeDesc(Pageable pageable, User user);
   public Reservation findFirstByOrderByIdDesc();
   //管理者画面表示用
   public Page<Reservation> findByRestaurantNameLike(String keyword, Pageable pageable);
   public Page<Reservation> findByRestaurantNameLikeAndReservedDatetimeBetween(String keyword, 
																			    LocalDateTime start, 
																			    LocalDateTime end, 
																			    Pageable pageable);
   public Page<Reservation> findAll(Pageable pageable);
   public Page<Reservation> findByReservedDatetimeBetween(LocalDateTime start, 
		   													LocalDateTime end, 
														    Pageable pageable);
   //CSV出力用
   public List<Reservation> findByRestaurantNameLike(String keyword);
   public List<Reservation> findByRestaurantNameLikeAndReservedDatetimeBetween(String keyword, 
																			    LocalDateTime start, 
																			    LocalDateTime end);
   public List<Reservation> findAll();
   public List<Reservation> findByReservedDatetimeBetween(LocalDateTime start, 
		   																	LocalDateTime end);
}