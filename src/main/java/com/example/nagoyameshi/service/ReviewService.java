package com.example.nagoyameshi.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReviewEditForm;
import com.example.nagoyameshi.form.ReviewRegisterForm;
import com.example.nagoyameshi.repository.ReviewRepository;

@Service
public class ReviewService {
	private final ReviewRepository reviewRepository;
	
	public ReviewService(ReviewRepository reviewRepository) {
		this.reviewRepository = reviewRepository;
	}
	
	//指定したidを持つレビューを取得する。
	public Optional<Review> findReviewById(Integer id) {
		return reviewRepository.findById(id);
	}
	
	//指定した店舗のすべてのレビューを作成日時が新しい順に並べ替え、ページングされた状態で取得する。
	public Page<Review> findReviewsByRestaurantOrderByCreatedAtDesc(Restaurant restaurant,Pageable pageable){
		return reviewRepository.findByRestaurantOrderByCreatedAtDesc(restaurant, pageable);
	}
	
	//レビューのレコード数を取得する。
	public Long countReviews() {
		return reviewRepository.count();
	}
	
	//idが最も大きいレビューを取得する。
	public Review findFirstReviewByOrderByIdDesc() {
		return reviewRepository.findFirstByOrderByIdDesc();
	}
	
	//フォームから送信されたレビューをデータベースに登録する。
	public void createReview(ReviewRegisterForm reviewRegisterForm, Restaurant restaurant, User user) {
		Review review = new Review();
		review.setScore(reviewRegisterForm.getScore());
		review.setContent(reviewRegisterForm.getContent());
		review.setRestaurant(restaurant);
		review.setUser(user);
		reviewRepository.save(review);
	}
	
	//フォームから送信されたレビューでデータベースを更新する。
	public void updateReview(ReviewEditForm reviewEditForm, Review review) {
		review.setScore(reviewEditForm.getScore());
		review.setContent(reviewEditForm.getContent());
		reviewRepository.save(review);
	}
	
	//指定したレビューをデータベースから削除する。
	public void deleteReview(Review review) {
		reviewRepository.delete(review);
	}
	
	//指定したユーザーが指定した店舗のレビューをすでに投稿済みかどうかをチェックする。
	public boolean hasUserAlreadyReviewed(Restaurant restaurant, User user) {
		return reviewRepository.findByRestaurantAndUser(restaurant, user) != null;
	}
}
