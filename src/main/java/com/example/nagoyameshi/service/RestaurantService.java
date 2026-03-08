package com.example.nagoyameshi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.form.RestaurantEditForm;
import com.example.nagoyameshi.form.RestaurantRegisterForm;
import com.example.nagoyameshi.repository.RestaurantRepository;

@Service
public class RestaurantService {
	private final RestaurantRepository restaurantRepository;
	
	public RestaurantService(RestaurantRepository restaurantRepository) {
		this.restaurantRepository = restaurantRepository;
	}
	
	public Page<Restaurant> findAllRestaurants(Pageable pageable){
		return restaurantRepository.findAll(pageable);
	}
	
	public Page<Restaurant> findRestaurantsByNameLike(String keyword, Pageable pageable){
		return restaurantRepository.findByNameLike("%"+keyword+"%", pageable);
	}
	
	public Optional<Restaurant> findRestaurantById(Integer id) {
		return restaurantRepository.findById(id);
	}
	
	public long countRestaurants() {
		return restaurantRepository.count();
	}
	
	public Restaurant findFirstRestaurantByOrderByIdDesc() {
		return restaurantRepository.findFirstByOrderByIdDesc();
	}
	
	@Transactional
	public void createRestaurant(RestaurantRegisterForm restaurantRegisterForm) {
		Restaurant restaurant = new Restaurant();
		
		restaurant.setName(restaurantRegisterForm.getName());
		
		MultipartFile imageFile = restaurantRegisterForm.getImageFile();
		if(!imageFile.isEmpty()) {
			// 画像ファイル名を生成してセットする
			String imageName = imageFile.getOriginalFilename();
			String hashedImageName = generateNewFileName(imageName);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
			copyImageFile(imageFile,filePath);
			restaurant.setImage(imageName);
		}
		
		restaurant.setDescription(restaurantRegisterForm.getDescription());
		restaurant.setLowestPrice(restaurantRegisterForm.getLowestPrice());
		restaurant.setHighestPrice(restaurantRegisterForm.getHighestPrice());
	    restaurant.setPostalCode(restaurantRegisterForm.getPostalCode());
	    restaurant.setAddress(restaurantRegisterForm.getAddress());
	    restaurant.setOpeningTime(restaurantRegisterForm.getOpeningTime());
	    restaurant.setClosingTime(restaurantRegisterForm.getClosingTime());
	    restaurant.setSeatingCapacity(restaurantRegisterForm.getSeatingCapacity());

	    restaurantRepository.save(restaurant);
	}
	
	@Transactional
	public void updateRestaurant(RestaurantEditForm restaurantEditForm, Restaurant restaurant){
		MultipartFile imageFile = restaurantEditForm.getImageFile();

	    if (!imageFile.isEmpty()) {
	        String imageName = imageFile.getOriginalFilename();
	        String hashedImageName = generateNewFileName(imageName);
	        Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
	        copyImageFile(imageFile, filePath);
	        restaurant.setImage(hashedImageName);
	    }

	    restaurant.setName(restaurantEditForm.getName());
	    restaurant.setDescription(restaurantEditForm.getDescription());
	    restaurant.setLowestPrice(restaurantEditForm.getLowestPrice());
	    restaurant.setHighestPrice(restaurantEditForm.getHighestPrice());
	    restaurant.setPostalCode(restaurantEditForm.getPostalCode());
	    restaurant.setAddress(restaurantEditForm.getAddress());
	    restaurant.setOpeningTime(restaurantEditForm.getOpeningTime());
	    restaurant.setClosingTime(restaurantEditForm.getClosingTime());
	    restaurant.setSeatingCapacity(restaurantEditForm.getSeatingCapacity());

	    restaurantRepository.save(restaurant);
	}
	
	@Transactional
	public void deleteRestaurant(Restaurant restaurant) {
		restaurantRepository.delete(restaurant);
	}
	
	public String generateNewFileName(String fileName) {
		//「.」区切りで文字列を配列形式で取り出す
        String[] fileNames = fileName.split("\\.");
        
        //拡張子（ping,jpegなど）の部分は変換しないのでlength-1
        for (int i = 0; i < fileNames.length - 1; i++) {
            fileNames[i] = UUID.randomUUID().toString();
        }

        String hashedFileName = String.join(".", fileNames);

        return hashedFileName;
	}
	
	public void copyImageFile(MultipartFile imageFile, Path filePath) {
        try {
            Files.copy(imageFile.getInputStream(), filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public boolean isValidPrices(Integer lowestPrice, Integer highestPrice) {
	    return highestPrice >= lowestPrice;
	}
	
	public boolean isValidBusinessHours(LocalTime openingTime, LocalTime closingTime) {
	    return closingTime.isAfter(openingTime);
	}
}
