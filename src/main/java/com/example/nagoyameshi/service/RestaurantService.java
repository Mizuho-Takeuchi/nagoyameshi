package com.example.nagoyameshi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;
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
    private final CategoryRestaurantService categoryRestaurantService;
    private final RegularHolidayRestaurantService regularHolidayRestaurantService;
	
	public RestaurantService(RestaurantRepository restaurantRepository,
							CategoryRestaurantService categoryRestaurantService,
							RegularHolidayRestaurantService regularHolidayRestaurantService) {
		this.restaurantRepository = restaurantRepository;
        this.categoryRestaurantService = categoryRestaurantService;
        this.regularHolidayRestaurantService = regularHolidayRestaurantService;
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
			restaurant.setImage(hashedImageName);
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
	    
	    if(restaurantRegisterForm.getCategoryIds() != null) {
	    	categoryRestaurantService.createCategoriesRestaurants(restaurantRegisterForm.getCategoryIds(), restaurant);
	    }
	    
	    if(restaurantRegisterForm.getRegularHolidayIds() != null) {
	    	regularHolidayRestaurantService.createRegularHolidaysRestaurants(restaurantRegisterForm.getRegularHolidayIds(), restaurant);
	    }
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
	    
	    categoryRestaurantService.syncCategoriesRestaurants(restaurantEditForm.getCategoryIds(), restaurant);
	    regularHolidayRestaurantService.syncRegularHolidaysRestaurants(restaurantEditForm.getRegularHolidayIds(), restaurant);
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
	
	public Page<Restaurant> findAllRestaurantsByOrderByCreatedAtDesc(Pageable pageable){
		return restaurantRepository.findAllByOrderByCreatedAtDesc(pageable);
	}
	
	// すべての店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する
    public Page<Restaurant> findAllRestaurantsByOrderByLowestPriceAsc(Pageable pageable) {
        return restaurantRepository.findAllByOrderByLowestPriceAsc(pageable);
    }

    // 指定されたキーワードを店舗名または住所またはカテゴリ名に含む店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する
    public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(Pageable pageable,String nameKeyword, String addressKeyword, String categoryNameKeyword) {
        return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(pageable,nameKeyword, addressKeyword, categoryNameKeyword);
    }

    // 指定されたキーワードを店舗名または住所またはカテゴリ名に含む店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する
    public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(Pageable pageable,String nameKeyword, String addressKeyword, String categoryNameKeyword) {
        return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(pageable,nameKeyword, addressKeyword, categoryNameKeyword);
    }

    // 指定されたidのカテゴリが設定された店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する
    public Page<Restaurant> findRestaurantsByCategoryIdOrderByCreatedAtDesc(Pageable pageable,Integer categoryId) {
        return restaurantRepository.findByCategoryIdOrderByCreatedAtDesc(pageable,categoryId);
    }

    // 指定されたidのカテゴリが設定された店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する
    public Page<Restaurant> findRestaurantsByCategoryIdOrderByLowestPriceAsc(Pageable pageable,Integer categoryId) {
        return restaurantRepository.findByCategoryIdOrderByLowestPriceAsc(pageable,categoryId);
    }

    // 指定された最低価格以下の店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する
    public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByCreatedAtDesc(Pageable pageable,Integer price) {
        return restaurantRepository.findByLowestPriceLessThanEqualOrderByCreatedAtDesc(pageable,price);
    }

    // 指定された最低価格以下の店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する
    public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByLowestPriceAsc(Pageable pageable,Integer price) {
        return restaurantRepository.findByLowestPriceLessThanEqualOrderByLowestPriceAsc(pageable,price);
    }
    
    //すべての店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する。
    public Page<Restaurant> findAllRestaurantsByOrderByAverageScoreDesc(Pageable pageable){
    	return restaurantRepository.findAllByOrderByAverageScoreDesc(pageable);
    }
    
    //指定されたキーワードを店舗名または住所またはカテゴリ名に含む店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する。
    public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(Pageable pageable, String nameKeyword, String addressKeyword, String categoryNameKeyword){
    	return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(pageable, nameKeyword, addressKeyword, categoryNameKeyword);
    }
    
    //指定されたidのカテゴリが設定された店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する。
    public Page<Restaurant> findRestaurantsByCategoryIdOrderByAverageScoreDesc(Pageable pageable, Integer id){
    	return restaurantRepository.findByCategoryIdOrderByAverageScoreDesc(pageable, id);
    }
    
    //指定された最低価格以下の店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する。
    public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByAverageScoreDesc(Pageable pageable, Integer price){
    	return restaurantRepository.findByLowestPriceLessThanEqualOrderByAverageScoreDesc(pageable, price);
    }
    
    //すべての店舗を予約数が多い順に並べ替え、ページングされた状態で取得する。
    public Page<Restaurant> findAllRestaurantsByOrderByReservationCountDesc(Pageable pageable){
    	return restaurantRepository.findAllByOrderByReservationCountDesc(pageable);
    }
    
    //指定されたキーワードを店舗名または住所またはカテゴリ名に含む店舗を予約数が多い順に並べ替え、
    //ページングされた状態で取得する。
    public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByReservationCountDesc(Pageable pageable,String nameKeyword, String addressKeyword, String categoryNameKeyword){
    	return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByReservationCountDesc(pageable, nameKeyword, addressKeyword, categoryNameKeyword);
    }
    
    //指定されたidのカテゴリが設定された店舗を予約数が多い順に並べ替え、
    //ページングされた状態で取得する。
    public Page<Restaurant> findRestaurantsByCategoryIdOrderByReservationCountDesc(Pageable pageable, Integer id){
    	return restaurantRepository.findByCategoryIdOrderByReservationCountDesc(pageable, id);
    }
    
    //指定された最低価格以下の店舗を予約数が多い順に並べ替え、ページングされた状態で取得する。
    public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByReservationCountDesc(Pageable pageable, Integer price){
    	return restaurantRepository.findByLowestPriceLessThanEqualOrderByReservationCountDesc(pageable, price);
    }
    
    //指定された店舗の定休日のday_indexフィールドの値をリストで取得する。
    public List<Integer> findDayIndexesByRestaurantId(Integer id){
    	return restaurantRepository.findDayIndexesByRestaurantId(id);
    }
}
