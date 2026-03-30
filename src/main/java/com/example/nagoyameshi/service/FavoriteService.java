package com.example.nagoyameshi.service;

import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.FavoriteRepository;

@Service
public class FavoriteService {
	private FavoriteRepository favoriteRepository;
	
	public FavoriteService(FavoriteRepository favoriteRepository) {
		this.favoriteRepository = favoriteRepository;
	}
	
	public Optional<Favorite> findFavoriteById(Integer id) {
		return favoriteRepository.findById(id);
	}
	
	public Favorite findFavoriteByRestaurantAndUser(Restaurant restaurant, User user) {
		return favoriteRepository.findByRestaurantAndUser(restaurant, user);
	}
	
	public Page<Favorite> findFavoritesByUserOrderByCreatedAtDesc(Pageable pageable, User user)	{
		return favoriteRepository.findByUserOrderByCreatedAtDesc(pageable, user);
	}
	
	public long countFavorites() {
		return favoriteRepository.count();
	}
	
	@Transactional
	public void createFavorite(Restaurant restaurant, User user) {
		Favorite favorite = new Favorite();
		favorite.setRestaurant(restaurant);
		favorite.setUser(user);	
		favoriteRepository.save(favorite);
	}
	
	@Transactional
	public void deleteFavorite(Favorite favorite) {
		favoriteRepository.delete(favorite);
	}
	
	public boolean isFavorite(Restaurant restaurant, User user) {
		return favoriteRepository.findByRestaurantAndUser(restaurant, user) != null;
	}
}
