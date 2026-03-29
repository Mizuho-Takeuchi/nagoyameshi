package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.nagoyameshi.entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {
	public Page<Restaurant> findByNameLike(String keyword, Pageable pageable);
	public Restaurant findFirstByOrderByIdDesc();
	public Page<Restaurant> findAllByOrderByCreatedAtDesc(Pageable pageable);
	public Page<Restaurant> findAllByOrderByLowestPriceAsc(Pageable pageable);
	@Query("SELECT DISTINCT r "
			+ "FROM Restaurant r "
			+ "LEFT JOIN r.categoriesRestaurants cr  "
			+ "WHERE r.name LIKE CONCAT('%',:name,'%') "
			+ "OR r.address LIKE CONCAT('%', :address,'%') "
			+ "OR cr.category.name LIKE CONCAT ('%', :categoryName, '%') "
			+ "ORDER BY r.createdAt DESC")
	public Page<Restaurant> findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(Pageable pageable, 
																							@Param("name")String name,
																							@Param("address")String address,
																							@Param("categoryName")String categoryName);
	
	@Query("SELECT DISTINCT r "
			+ "FROM Restaurant r "
			+ "LEFT JOIN r.categoriesRestaurants cr  "
			+ "WHERE r.name LIKE CONCAT('%',:name,'%') "
			+ "OR r.address LIKE CONCAT('%', :address,'%') "
			+ "OR cr.category.name LIKE CONCAT ('%', :categoryName, '%') "
			+ "ORDER BY r.lowestPrice DESC")
	public Page<Restaurant> findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(Pageable pageable,
																							@Param("name")String name,
																							@Param("address")String address,
																							@Param("categoryName")String categoryName);	
	
	@Query("SELECT DISTINCT r "
			+ "FROM Restaurant r "
			+ "INNER JOIN r.categoriesRestaurants cr "
			+ "WHERE cr.category.id = :id "
			+ "ORDER BY r.createdAt DESC")
	public Page<Restaurant> findByCategoryIdOrderByCreatedAtDesc(Pageable pageable, @Param("id")Integer id);

	@Query("SELECT DISTINCT r "
			+ "FROM Restaurant r "
			+ "INNER JOIN r.categoriesRestaurants cr "
			+ "WHERE cr.category.id = :id "
			+ "ORDER BY r.lowestPrice DESC")
	public Page<Restaurant> findByCategoryIdOrderByLowestPriceAsc(Pageable pageable, @Param("id")Integer id);
	public Page<Restaurant> findByLowestPriceLessThanEqualOrderByCreatedAtDesc(Pageable pageable, Integer price);
	public Page<Restaurant> findByLowestPriceLessThanEqualOrderByLowestPriceAsc(Pageable pageable, Integer price);
	
	@Query("SELECT r FROM Restaurant r "
			+ "LEFT OUTER JOIN r.reviews v "
			+ "GROUP BY r.id "
			+ "ORDER BY AVG(v.score) DESC")
	public Page<Restaurant> findAllByOrderByAverageScoreDesc(Pageable pageable);
	
	@Query("SELECT r FROM Restaurant r "
			+ "INNER JOIN r.categoriesRestaurants cr "
			+ "LEFT OUTER JOIN r.reviews v "
			+ "WHERE r.name LIKE CONCAT('%',:name,'%') "
			+ "OR r.address LIKE CONCAT('%', :address,'%') "
			+ "OR cr.category.name LIKE CONCAT ('%', :categoryName, '%') "
			+ "GROUP BY r.id "
			+ "ORDER BY AVG(v.score) DESC")
	public Page<Restaurant> findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(Pageable pageable,
																								@Param("name") String name,
																								@Param("address")String address,
																								@Param("categoryName")String categoryName);
	@Query("SELECT r FROM Restaurant r "
			+ "INNER JOIN r.categoriesRestaurants cr "
			+ "LEFT OUTER JOIN r.reviews v "
			+ "WHERE cr.category.id = :id "
			+ "GROUP BY r.id "
			+ "ORDER BY AVG(v.score) DESC")
	public Page<Restaurant> findByCategoryIdOrderByAverageScoreDesc(Pageable pageable, @Param("id") Integer id);
	
	@Query("SELECT r FROM Restaurant r "
			+ "LEFT OUTER JOIN r.reviews v "
			+ "WHERE r.lowestPrice <= :price "
			+ "GROUP BY r.id "
			+ "ORDER BY AVG(v.score) DESC")
	public Page<Restaurant> findByLowestPriceLessThanEqualOrderByAverageScoreDesc(Pageable pageable, @Param("price")Integer price);
	
	@Query("SELECT r FROM Restaurant r "
			+ "LEFT OUTER JOIN r.reservations rs "
			+ "GROUP BY r.id "
			+ "ORDER BY COUNT(rs) DESC")
	public Page<Restaurant> findAllByOrderByReservationCountDesc(Pageable pageable);
	
	@Query("SELECT r FROM Restaurant r "
			+ "LEFT JOIN r.categoriesRestaurants cr "
			+ "LEFT OUTER JOIN r.reservations rs "
			+ "WHERE r.name LIKE CONCAT('%', :name, '%') "
			+ "OR r.address LIKE CONCAT('%', :address,'%') "
			+ "OR cr.category.name LIKE CONCAT ('%', :categoryName, '%') "
			+ "GROUP BY r.id "
			+ "ORDER BY COUNT (DISTINCT rs.id) DESC")
	public Page<Restaurant> findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByReservationCountDesc(Pageable pageable,
																									@Param("name") String name,
																									@Param("address") String address,
																									@Param("categoryName") String categoryName);
	
	@Query("SELECT r FROM Restaurant r "
			+ "INNER JOIN r.categoriesRestaurants cr "
			+ "LEFT OUTER JOIN r.reservations rs "
			+ "WHERE cr.category = :id "
			+ "GROUP BY r.id "
			+ "ORDER BY COUNT(rs) DESC")
	public Page<Restaurant> findByCategoryIdOrderByReservationCountDesc(Pageable pageable, @Param("id") Integer id);
	
	@Query("SELECT r FROM Restaurant r "
			+ "LEFT OUTER JOIN r.reservations rs "
			+ "WHERE r.lowestPrice <= :price "
			+ "GROUP BY r.id "
			+ "ORDER BY COUNT(rs) DESC")
	public Page<Restaurant> findByLowestPriceLessThanEqualOrderByReservationCountDesc(Pageable pageable, @Param("price") Integer price);
	
	@Query("SELECT rh.dayIndex FROM RegularHoliday rh "
			+ "INNER JOIN rh.regularHolidaysRestaurants rhr "
			+ "INNER JOIN rhr.restaurant r "
			+ "WHERE r.id = :id")
	public List<Integer> findDayIndexesByRestaurantId(@Param("id")Integer id);
}
