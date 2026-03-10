package com.example.nagoyameshi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.CategoryRestaurant;
import com.example.nagoyameshi.entity.Restaurant;

public interface CategoryRestaurantRepository extends JpaRepository<CategoryRestaurant, Integer>{
	@Query("SELECT c.category.id FROM CategoryRestaurant c WHERE c.restaurant = :restaurant ORDER BY c.id ASC")
	List<Integer> findCategoryIdsByRestaurantOrderByIdAsc(@Param("restaurant") Restaurant restaurant);
	
	Optional<CategoryRestaurant> findByRestaurantAndCategory(Restaurant restaurant, Category category);
	
	List<CategoryRestaurant> findByRestaurantOrderByIdAsc (Restaurant restaurant);
}
