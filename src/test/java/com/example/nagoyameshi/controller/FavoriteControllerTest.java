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

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.service.FavoriteService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FavoriteControllerTest {
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private FavoriteService favoriteService;
	
	//お気に入り一覧ページ
	@Test
	public void 未ログインの場合はお気に入り一覧ページからログインページにリダイレクトする() throws Exception{
		mockMvc.perform(get("/favorites"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 無料会員としてログイン済みの場合はお気に入り一覧ページから有料プラン登録ページにリダイレクトする() throws Exception{
		mockMvc.perform(get("/favorites"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/subscription/register"));
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	public void 有料会員としてログイン済みの場合はお気に入り一覧ページが正しく表示される() throws Exception{
		mockMvc.perform(get("/favorites"))
				.andExpect(status().isOk())
				.andExpect(view().name("favorites/index"));
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合はお気に入り一覧ページが表示されずに403エラーが発生する() throws Exception{
		mockMvc.perform(get("/favorites"))
				.andExpect(status().isForbidden());
	}
	
	//お気に入り追加機能
	@Test
	@Transactional
	private void 未ログインの場合はお気に入りに追加せずにログインページにリダイレクトする() throws Exception{
		long contBofore = favoriteService.countFavorites();
		
		mockMvc.perform(post("/restaurants/1/favorites/create").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
		
		long countAfter = favoriteService.countFavorites();
		assertThat(contBofore).isEqualTo(countAfter);
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Transactional
	private void 無料会員としてログイン済みの場合はお気に入りに追加せずに有料プラン登録ページにリダイレクトする() throws Exception{
		long contBofore = favoriteService.countFavorites();
		
		mockMvc.perform(post("/restaurants/1/favorites/create").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/subscription/register"));
		
		long countAfter = favoriteService.countFavorites();
		assertThat(contBofore).isEqualTo(countAfter);
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	@Transactional
	public void 有料会員としてログイン済みの場合はお気に入り追加後に店舗詳細ページにリダイレクトする() throws Exception{
		long contBofore = favoriteService.countFavorites();
		
		mockMvc.perform(post("/restaurants/1/favorites/create").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/restaurants/1"));
		
		long countAfter = favoriteService.countFavorites();
		assertThat(contBofore).isEqualTo(countAfter - 1);
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	@Transactional
	public void 管理者としてログイン済みの場合はお気に入りに追加せずに403エラーが発生する() throws Exception{
		long contBofore = favoriteService.countFavorites();
		
		mockMvc.perform(post("/restaurants/1/favorites/create").with(csrf()))
				.andExpect(status().isForbidden());
		
		long countAfter = favoriteService.countFavorites();
		assertThat(contBofore).isEqualTo(countAfter);
	}
	
	//お気に入り解除機能
	@Test
	@Transactional
	public void 未ログインの場合はお気に入りを解除せずにログインページにリダイレクトする() throws Exception{
		mockMvc.perform(post("/favorites/1/delete").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
		Optional<Favorite> optionalFavorite = favoriteService.findFavoriteById(1);
		assertThat(optionalFavorite).isPresent();
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Transactional
	private void 無料会員としてログイン済みの場合はお気に入りを解除せずに有料プラン登録ページにリダイレクトする() throws Exception{
		mockMvc.perform(post("/favorites/1/delete").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/subscription/register"));
		Optional<Favorite> optionalFavorite = favoriteService.findFavoriteById(1);
		assertThat(optionalFavorite).isPresent();
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	@Transactional
	public void 有料会員としてログイン済みの場合は自身のお気に入り解除後にお気に入り一覧ページにリダイレクトする() throws Exception{
		mockMvc.perform(post("/favorites/1/delete").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/favorites"));
		Optional<Favorite> optionalFavorite = favoriteService.findFavoriteById(1);
		assertThat(optionalFavorite).isEmpty();
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	@Transactional
	public void 有料会員としてログイン済みの場合は他人のお気に入りを解除せずにお気に入り一覧ページにリダイレクトする() throws Exception{
		mockMvc.perform(post("/favorites/21/delete").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/favorites"));
		Optional<Favorite> optionalFavorite = favoriteService.findFavoriteById(1);
		assertThat(optionalFavorite).isPresent();
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	@Transactional
	public void 管理者としてログイン済みの場合はお気に入りを解除せずに403エラーが発生する() throws Exception{
		mockMvc.perform(post("/favorites/1/delete").with(csrf()))
				.andExpect(status().isForbidden());
		Optional<Favorite> optionalFavorite = favoriteService.findFavoriteById(1);
		assertThat(optionalFavorite).isPresent();
	}
}
