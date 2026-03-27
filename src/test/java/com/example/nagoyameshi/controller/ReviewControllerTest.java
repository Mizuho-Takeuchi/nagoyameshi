package com.example.nagoyameshi.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.service.ReviewService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReviewControllerTest {
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ReviewService reviewService;
	
	//レビュー一覧ページ
	@Test
	public void 未ログインの場合はレビュー一覧ページからログインページにリダイレクトする() throws Exception{
		mockMvc.perform(get("/restaurants/1/reviews"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 無料会員としてログイン済みの場合はレビュー一覧ページが正しく表示される() throws Exception{
		mockMvc.perform(get("/restaurants/1/reviews"))
				.andExpect(status().isOk())
				.andExpect(view().name("reviews/index"));
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	public void 有料会員としてログイン済みの場合はレビュー一覧ページが正しく表示される() throws Exception{
		mockMvc.perform(get("/restaurants/1/reviews"))
				.andExpect(status().isOk())
				.andExpect(view().name("reviews/index"));
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合はレビュー一覧ページが表示されずに403エラーが発生する() throws Exception{
		mockMvc.perform(get("/restaurants/1/reviews"))
				.andExpect(status().isForbidden());
	}
	
	//レビュー投稿ページ
	@Test
	public void 未ログインの場合はレビュー投稿ページからログインページにリダイレクトする() throws Exception{
		mockMvc.perform(get("/restaurants/1/reviews/register"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 無料会員としてログイン済みの場合はレビュー投稿ページから有料プラン登録ページにリダイレクトする() throws Exception{
		mockMvc.perform(get("/restaurants/1/reviews/register"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/subscription/register"));
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	public void 有料会員としてログイン済みの場合はレビュー投稿ページが正しく表示される() throws Exception{
		mockMvc.perform(get("/restaurants/1/reviews/register"))
				.andExpect(status().isOk())
				.andExpect(view().name("reviews/register"));
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合はレビュー投稿ページが表示されずに403エラーが発生する() throws Exception{
		mockMvc.perform(get("/restaurants/1/reviews/register"))
				.andExpect(status().isForbidden());
	}
	
	//レビュー投稿機能
	@Test
	@Transactional
	public void 未ログインの場合はレビューを登録せずにログインページにリダイレクトする() throws Exception{
		long countBefore = reviewService.countReviews();
		
		mockMvc.perform(post("/restaurants/1/reviews/create").with(csrf())
				.param("score", "5")
				.param("content", "美味"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login"));
		
		long countAfter = reviewService.countReviews();
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Transactional
	public void 無料会員としてログイン済みの場合はレビューを登録せずに有料プラン登録ページにリダイレクトする() throws Exception{
		long countBefore = reviewService.countReviews();
		
		mockMvc.perform(post("/restaurants/1/reviews/create").with(csrf())
				.param("score", "5")
				.param("content", "美味"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/subscription/register"));
		
		long countAfter = reviewService.countReviews();
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	@Transactional
	public void 有料会員としてログイン済みの場合はレビュー投稿後に店舗詳細ページにリダイレクトする() throws Exception{
		long countBefore = reviewService.countReviews();
		
		mockMvc.perform(post("/restaurants/1/reviews/create").with(csrf())
				.param("score", "5")
				.param("content", "美味"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/restaurants/1"));
		
		long countAfter = reviewService.countReviews();
		assertThat(countAfter).isEqualTo(countBefore + 1);
		
		Review review = reviewService.findFirstReviewByOrderByIdDesc();
		assertThat(review.getScore()).isEqualTo(5);
		assertThat(review.getContent()).isEqualTo("美味");
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	@Transactional
	public void 管理者としてログイン済みの場合はレビューを投稿せずに403エラーが発生する() throws Exception{
		long countBefore = reviewService.countReviews();
		
		mockMvc.perform(post("/restaurants/1/reviews/create").with(csrf())
				.param("score", "5")
				.param("content", "美味"))
			.andExpect(status().isForbidden());
		
		long countAfter = reviewService.countReviews();
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	//レビュー編集ページ
	@Test
	public void 未ログインの場合はレビュー編集ページからログインページにリダイレクトする() throws Exception{
		mockMvc.perform(get("/restaurants/2/reviews/1/edit"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 無料会員としてログイン済みの場合はレビュー編集ページから有料プラン登録ページにリダイレクトする() throws Exception{
		mockMvc.perform(get("/restaurants/2/reviews/1/edit"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/subscription/register"));
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	public void 有料会員としてログイン済みの場合は自身のレビュー編集ページが正しく表示される() throws Exception{
		mockMvc.perform(get("/restaurants/2/reviews/1/edit"))
				.andExpect(status().isOk())
				.andExpect(view().name("reviews/edit"));
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	public void 有料会員としてログイン済みの場合は他人のレビュー編集ページから店舗詳細ページにリダイレクトする() throws Exception{
		mockMvc.perform(get("/restaurants/2/reviews/2/edit"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/restaurants/2"));
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合はレビュー編集ページが表示されずに403エラーが発生する() throws Exception{
		mockMvc.perform(get("/restaurants/2/reviews/1/edit"))
				.andExpect(status().isForbidden());
	}
	
	//レビュー更新機能
	@Test
	@Transactional
	public void 未ログインの場合はレビューを更新せずにログインページにリダイレクトする() throws Exception{
		mockMvc.perform(post("/restaurants/2/reviews/1/update").with(csrf())
				.param("score", "1")
				.param("content", "最悪"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login"));
		
		Optional<Review> optionalReview = reviewService.findReviewById(1);
	    assertThat(optionalReview).isPresent();
	    Review review = optionalReview.get();
	    assertThat(review.getScore()).isEqualTo(3);
	    assertThat(review.getContent()).isEqualTo("名古屋では有名な格安で焼肉食べ放題のお店。タイミングよく仕事で行く機会があったので、地元の友人と一緒に来店しました。店内は広くゆったりとできます。");
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Transactional
	public void 無料会員としてログイン済みの場合はレビューを更新せずに有料プラン登録ページにリダイレクトする() throws Exception{
		mockMvc.perform(post("/restaurants/2/reviews/1/update").with(csrf())
				.param("score", "1")
				.param("content", "最悪"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/subscription/register"));
		
		Optional<Review> optionalReview = reviewService.findReviewById(1);
	    assertThat(optionalReview).isPresent();
	    Review review = optionalReview.get();
	    assertThat(review.getScore()).isEqualTo(3);
	    assertThat(review.getContent()).isEqualTo("名古屋では有名な格安で焼肉食べ放題のお店。タイミングよく仕事で行く機会があったので、地元の友人と一緒に来店しました。店内は広くゆったりとできます。");
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	@Transactional
	public void 有料会員としてログイン済みの場合は自身のレビュー更新後に店舗詳細ページにリダイレクトする() throws Exception{
		mockMvc.perform(post("/restaurants/2/reviews/1/update").with(csrf())
				.param("score", "1")
				.param("content", "最悪"))
			.andExpect(status().is3xxRedirection())
	        .andExpect(redirectedUrl("/restaurants/2"));
		
		Optional<Review> optionalReview = reviewService.findReviewById(1);
	    assertThat(optionalReview).isPresent();
	    Review review = optionalReview.get();
	    assertThat(review.getScore()).isEqualTo(1);
	    assertThat(review.getContent()).isEqualTo("最悪");
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	@Transactional
	public void 有料会員としてログイン済みの場合は他人のレビューを更新せずに店舗詳細ページにリダイレクトする() throws Exception{
		mockMvc.perform(post("/restaurants/2/reviews/2/update").with(csrf())
				.param("score", "1")
				.param("content", "最悪"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/restaurants/2"));
		
		Optional<Review> optionalReview = reviewService.findReviewById(1);
	    assertThat(optionalReview).isPresent();
	    Review review = optionalReview.get();
	    assertThat(review.getScore()).isEqualTo(3);
	    assertThat(review.getContent()).isEqualTo("名古屋では有名な格安で焼肉食べ放題のお店。タイミングよく仕事で行く機会があったので、地元の友人と一緒に来店しました。店内は広くゆったりとできます。");
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	@Transactional
	public void 管理者としてログイン済みの場合はレビューを更新せずに403エラーが発生する() throws Exception{
		mockMvc.perform(post("/restaurants/2/reviews/1/update").with(csrf())
				.param("score", "1")
				.param("content", "最悪"))
			.andExpect(status().isForbidden());
		
		Optional<Review> optionalReview = reviewService.findReviewById(1);
	    assertThat(optionalReview).isPresent();
	    Review review = optionalReview.get();
	    assertThat(review.getScore()).isEqualTo(3);
	    assertThat(review.getContent()).isEqualTo("名古屋では有名な格安で焼肉食べ放題のお店。タイミングよく仕事で行く機会があったので、地元の友人と一緒に来店しました。店内は広くゆったりとできます。");
	}
	
	//レビュー削除機能
	@Test
	@Transactional
	public void 未ログインの場合はレビューを削除せずにログインページにリダイレクトする() throws Exception{
		mockMvc.perform(post("/restaurants/2/reviews/1/delete").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
		Optional<Review> optionalReview = reviewService.findReviewById(1);
		assertThat(optionalReview).isPresent();
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Transactional
	public void 無料会員としてログイン済みの場合はレビューを削除せずに有料プラン登録ページにリダイレクトする() throws Exception{
		mockMvc.perform(post("/restaurants/2/reviews/1/delete").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/subscription/register"));
		Optional<Review> optionalReview = reviewService.findReviewById(1);
		assertThat(optionalReview).isPresent();
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	@Transactional
	public void 有料会員としてログイン済みの場合は自身のレビュー削除後に店舗詳細ページにリダイレクトする() throws Exception{
		mockMvc.perform(post("/restaurants/2/reviews/1/delete").with(csrf()))
				.andExpect(status().is3xxRedirection())
		        .andExpect(redirectedUrl("/restaurants/2"));
		Optional<Review> optionalReview = reviewService.findReviewById(1);
		assertThat(optionalReview).isEmpty();
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	@Transactional
	public void 有料会員としてログイン済みの場合は他人のレビューを削除せずに店舗詳細ページにリダイレクトする() throws Exception{
		mockMvc.perform(post("/restaurants/2/reviews/2/delete").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/restaurants/2"));
		Optional<Review> optionalReview = reviewService.findReviewById(1);
		assertThat(optionalReview).isPresent();
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	@Transactional
	public void 管理者としてログイン済みの場合はレビューを削除せずに403エラーが発生する() throws Exception{
		mockMvc.perform(post("/restaurants/2/reviews/1/edit").with(csrf()))
				.andExpect(status().isForbidden());
		Optional<Review> optionalReview = reviewService.findReviewById(1);
		assertThat(optionalReview).isPresent();
	}
}
