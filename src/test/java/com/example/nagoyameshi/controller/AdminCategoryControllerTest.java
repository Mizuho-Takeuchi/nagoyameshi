package com.example.nagoyameshi.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.service.CategoryService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminCategoryControllerTest {
   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private CategoryService categoryService;

   @Test
   public void 未ログインの場合は管理者用のカテゴリ一覧ページからログインページにリダイレクトする() throws Exception {
       mockMvc.perform(get("/admin/categories"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/login"));
   }

   @Test
   @WithUserDetails("taro.samurai@example.com")
   public void 一般ユーザーとしてログイン済みの場合は管理者用のカテゴリ一覧ページが表示されずに403エラーが発生する() throws Exception {
       mockMvc.perform(get("/admin/categories"))
              .andExpect(status().isForbidden());
   }

   @Test
   @WithUserDetails("hanako.samurai@example.com")
   public void 管理者としてログイン済みの場合は管理者用のカテゴリ一覧ページが正しく表示される() throws Exception {
       mockMvc.perform(get("/admin/categories"))
          .andExpect(status().isOk())
          .andExpect(view().name("admin/categories/index"));
   }

   @Test
   @Transactional
   public void 未ログインの場合はカテゴリを登録せずにログインページにリダイレクトする() throws Exception {
       // テスト前のレコード数を取得する
   long countBefore = categoryService.countCategories();

   mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/categories/create")
           .with(csrf())
           .param("name", "テストカテゴリ"))
       .andExpect(status().is3xxRedirection())
       .andExpect(redirectedUrl("/login"));

   // テスト後のレコード数を取得する
   long countAfter = categoryService.countCategories();

   // レコード数が変わっていないことを検証する
       assertThat(countAfter).isEqualTo(countBefore);
   }

   @Test
   @WithUserDetails("taro.samurai@example.com")
   @Transactional
   public void 一般ユーザーとしてログイン済みの場合はカテゴリを登録せずに403エラーが発生する() throws Exception {
       // テスト前のレコード数を取得する
   long countBefore = categoryService.countCategories();

   mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/categories/create")
           .with(csrf())
           .param("name", "テストカテゴリ"))
       .andExpect(status().isForbidden());

   // テスト後のレコード数を取得する
   long countAfter = categoryService.countCategories();

   // レコード数が変わっていないことを検証する
       assertThat(countAfter).isEqualTo(countBefore);
   }
   
   @Test
   @WithUserDetails("hanako.samurai@example.com")
   @Transactional
   public void 管理者としてログイン済みの場合はカテゴリ登録後にカテゴリ一覧ページにリダイレクトする() throws Exception {
       // テスト前のレコード数を取得する
   long countBefore = categoryService.countCategories();

   mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/categories/create")
           .with(csrf())
           .param("name", "テストカテゴリ"))
       .andExpect(status().is3xxRedirection())
       .andExpect(redirectedUrl("/admin/categories"));

   // テスト後のレコード数を取得する
   long countAfter = categoryService.countCategories();

   // レコード数が1つ増加していることを検証する
   assertThat(countAfter).isEqualTo(countBefore + 1);

   Category category = categoryService.findFirstCategoryByOrderByIdDesc();
   assertThat(category.getName()).isEqualTo("テストカテゴリ");
   }

   @Test
   @Transactional
   public void 未ログインの場合はカテゴリを更新せずにログインページにリダイレクトする() throws Exception {

   mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/categories/1/update")
           .with(csrf())
           .param("name", "テストカテゴリ"))
       .andExpect(status().is3xxRedirection())
       .andExpect(redirectedUrl("/login"));

   Optional<Category> optionalCategory = categoryService.findCategoryById(1);
   assertThat(optionalCategory).isPresent();
   Category category = optionalCategory.get();
   assertThat(category.getName()).isEqualTo("居酒屋");
   }

   @Test
   @WithUserDetails("taro.samurai@example.com")
   @Transactional
   public void 一般ユーザーとしてログイン済みの場合はカテゴリを更新せずに403エラーが発生する() throws Exception {
   mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/categories/1/update")
           .with(csrf())
           .param("name", "テストカテゴリ"))
       .andExpect(status().isForbidden());

   Optional<Category> optionalCategory = categoryService.findCategoryById(1);
   assertThat(optionalCategory).isPresent();
   Category category = optionalCategory.get();
   assertThat(category.getName()).isEqualTo("居酒屋");
   }

   @Test
   @WithUserDetails("hanako.samurai@example.com")
   @Transactional
   public void 管理者としてログイン済みの場合はカテゴリ更新後にカテゴリ一覧ページにリダイレクトする() throws Exception {
   mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/categories/1/update")
		   .with(csrf())
           .param("name", "テストカテゴリ"))
       .andExpect(status().is3xxRedirection())
       .andExpect(redirectedUrl("/admin/categories"));

   Optional<Category> optionalCategory = categoryService.findCategoryById(1);
   assertThat(optionalCategory).isPresent();
   Category category = optionalCategory.get();
   assertThat(category.getName()).isEqualTo("テストカテゴリ");
   }

   @Test
   @Transactional
   public void 未ログインの場合はカテゴリを削除せずにログインページにリダイレクトする() throws Exception {
       mockMvc.perform(post("/admin/categories/1/delete").with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/login"));
       
       Optional<Category> optionalCategory = categoryService.findCategoryById(1);
       assertThat(optionalCategory).isPresent();
       }

   @Test
   @WithUserDetails("taro.samurai@example.com")
   @Transactional
   public void 一般ユーザーとしてログイン済みの場合はカテゴリを削除せずに403エラーが発生する() throws Exception {
       mockMvc.perform(post("/admin/categories/1/delete").with(csrf()))
              .andExpect(status().isForbidden());

       Optional<Category> optionalCategory = categoryService.findCategoryById(1);
       assertThat(optionalCategory).isPresent();
       }

   @Test
   @WithUserDetails("hanako.samurai@example.com")
   @Transactional
   public void 管理者としてログイン済みの場合はカテゴリ削除後にカテゴリ一覧ページにリダイレクトする() throws Exception {
       mockMvc.perform(post("/admin/categories/1/delete").with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/admin/categories"));

       Optional<Category> optionalCategory = categoryService.findCategoryById(1);
       assertThat(optionalCategory).isEmpty();
       }
}
