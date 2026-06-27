package com.example.nagoyameshi.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
   public Page<Reservation> findByUserOrderByReservedDatetimeDesc(Pageable pageable, User user);
   public Reservation findFirstByOrderByIdDesc();
   //管理者画面表示用
   public Page<Reservation> findByRestaurantNameLikeOrderByReservedDatetimeDesc(String keyword, Pageable pageable);
   public Page<Reservation> findByRestaurantNameLikeAndReservedDatetimeBetweenOrderByReservedDatetimeDesc(String keyword, 
																			    LocalDateTime start, 
																			    LocalDateTime end, 
																			    Pageable pageable);
   public Page<Reservation> findAllByOrderByReservedDatetimeDesc(Pageable pageable);
   public Page<Reservation> findByReservedDatetimeBetweenOrderByReservedDatetimeDesc(LocalDateTime start, 
		   													LocalDateTime end, 
														    Pageable pageable);
   //CSV出力用
   public List<Reservation> findByRestaurantNameLikeOrderByReservedDatetimeDesc(String keyword);
   public List<Reservation> findByRestaurantNameLikeAndReservedDatetimeBetweenOrderByReservedDatetimeDesc(String keyword, 
																			    LocalDateTime start, 
																			    LocalDateTime end);
   public List<Reservation> findAllByOrderByReservedDatetimeDesc();
   public List<Reservation> findByReservedDatetimeBetweenOrderByReservedDatetimeDesc(LocalDateTime start, 
		   																	LocalDateTime end);
   
   //店舗管理者画面表示用
   public long countByRestaurant(Restaurant restaurant);
   public Page<Reservation> findByRestaurantOrderByReservedDatetimeDesc(Restaurant restaurant, Pageable pageable);
   public long countByRestaurantAndReservedDatetimeBetween(Restaurant restaurant, LocalDateTime start, LocalDateTime end);
   public Page<Reservation> findByRestaurantAndReservedDatetimeBetween(Restaurant restaurant, LocalDateTime start, LocalDateTime end, Pageable pageable);
}