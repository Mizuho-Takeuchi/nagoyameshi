package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.CategoryRestaurant;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.CategoryRestaurantRepository;

@Service
public class CategoryRestaurantService {
	private final CategoryRestaurantRepository categoryRestaurantRepository;
	private final CategoryService categoryService;
	
	public CategoryRestaurantService(CategoryRestaurantRepository categoryRestaurantRepository,
									CategoryService categoryService) {
		this.categoryRestaurantRepository = categoryRestaurantRepository;
		this.categoryService = categoryService;
	}

	public List<Integer> findCategoryIdsByRestaurantOrderByIdAsc(Restaurant restaurant){
		return categoryRestaurantRepository.findCategoryIdsByRestaurantOrderByIdAsc(restaurant);
	}
	
	@Transactional
	public void createCategoriesRestaurants(List<Integer> listInteger, Restaurant restaurant) {
		for(Integer i : listInteger) {
			if(i != null) {
				Optional<Category> optionalCategory = categoryService.findCategoryById(i);
				
				if(optionalCategory.isPresent()) {
					Category category = optionalCategory.get();
					Optional<CategoryRestaurant> optionalCategoryRestaurant = categoryRestaurantRepository.findByRestaurantAndCategory(restaurant, category);
					
					if(optionalCategoryRestaurant.isEmpty()) {
						CategoryRestaurant categoryRestaurant = optionalCategoryRestaurant.get();
						categoryRestaurant.setRestaurant(restaurant);	
						categoryRestaurant.setCategory(category);
						
						categoryRestaurantRepository.save(categoryRestaurant);
					}
				}
			}
		}
	}
	
	@Transactional
	public void syncCategoriesRestaurants(List<Integer> newCategoryIds, Restaurant restaurant) {
		//既存の紐づけ
		List<CategoryRestaurant> currentCategoriesRestaurants = categoryRestaurantRepository.findByRestaurantOrderByIdAsc(restaurant);
				
		//フォームから送信されたカテゴリのidのリストがnullの場合
		if(newCategoryIds == null) {
			for(CategoryRestaurant currentCategoryRestaurant : currentCategoriesRestaurants) {
				categoryRestaurantRepository.delete(currentCategoryRestaurant);
			}
		}else {
			//既存の紐づけがフォームから送信されたカテゴリのidのリストに含まれていない場合	
			for(CategoryRestaurant currentCategoryRestaurant : currentCategoriesRestaurants) {
				if(!newCategoryIds.contains(currentCategoryRestaurant.getCategory().getId())) {
					categoryRestaurantRepository.delete(currentCategoryRestaurant);
				}
			}
			
			//フォームから送信されたカテゴリのidが既存の紐づけに存在しない場合
			for(Integer newCategoryId : newCategoryIds) {
				if(newCategoryId != null) {
					Optional<Category> optionalCategory = categoryService.findCategoryById(newCategoryId);
					
					if(optionalCategory.isPresent()) {
						Category category = optionalCategory.get();
						Optional<CategoryRestaurant> optionalCurrentCategoryRestaurant = categoryRestaurantRepository.findByRestaurantAndCategory(restaurant, category);
						
						if(optionalCurrentCategoryRestaurant.isEmpty()) {
							CategoryRestaurant categoryRestaurant = new CategoryRestaurant();
							categoryRestaurant.setRestaurant(restaurant);
							categoryRestaurant.setCategory(category);		
							categoryRestaurantRepository.save(categoryRestaurant);
						}
					}
				}
			}
		}
	}
}
