package com.example.nagoyameshi.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Term;
import com.example.nagoyameshi.service.TermService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminTermControllerTest {
   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private TermService termService;

   @Test
   public void 未ログインの場合は管理者用の利用規約ページからログインページにリダイレクトする() throws Exception {
       mockMvc.perform(get("/admin/terms"))
              .andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/login"));
   }

   @Test
   @WithUserDetails("taro.samurai@example.com")
   public void 一般ユーザーとしてログイン済みの場合は管理者用の利用規約ページが表示されずに403エラーが発生する() throws Exception {
       mockMvc.perform(get("/admin/terms"))
              .andExpect(status().isForbidden());
   }

   @Test
   @WithUserDetails("hanako.samurai@example.com")
   public void 管理者としてログイン済みの場合は管理者用の利用規約ページが正しく表示される() throws Exception {
       mockMvc.perform(get("/admin/terms"))
              .andExpect(status().isOk())
              .andExpect(view().name("admin/terms/index"));
   }

   @Test
   public void 未ログインの場合は管理者用の利用規約編集ページからログインページにリダイレクトする() throws Exception {
       mockMvc.perform(get("/admin/terms/edit"))
              .andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/login"));
   }

   @Test
   @WithUserDetails("taro.samurai@example.com")
   public void 一般ユーザーとしてログイン済みの場合は管理者用の利用規約編集ページが表示されずに403エラーが発生する() throws Exception {
       mockMvc.perform(get("/admin/terms/edit"))
              .andExpect(status().isForbidden());
   }

   @Test
   @WithUserDetails("hanako.samurai@example.com")
   public void 管理者としてログイン済みの場合は管理者用の利用規約編集ページが正しく表示される() throws Exception {
       mockMvc.perform(get("/admin/terms/edit"))
              .andExpect(status().isOk())
              .andExpect(view().name("admin/terms/edit"));
   }

   @Test
   @Transactional
   public void 未ログインの場合は利用規約を更新せずにログインページにリダイレクトする() throws Exception {
       Term term = termService.findFirstTermByOrderByIdDesc();
       String originalContent = term.getContent();

       mockMvc.perform(post("/admin/terms/update").with(csrf()).param("content", "テスト内容"))
              .andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/login"));

       term = termService.findFirstTermByOrderByIdDesc();
       assertThat(term.getContent()).isEqualTo(originalContent);
   }

   @Test
   @WithUserDetails("taro.samurai@example.com")
   @Transactional
   public void 一般ユーザーとしてログイン済みの場合は利用規約を更新せずに403エラーが発生する() throws Exception {
       Term term = termService.findFirstTermByOrderByIdDesc();
       String originalContent = term.getContent();

       mockMvc.perform(post("/admin/terms/update").with(csrf()).param("content", "テスト内容"))
              .andExpect(status().isForbidden());

       term = termService.findFirstTermByOrderByIdDesc();
       assertThat(term.getContent()).isEqualTo(originalContent);
   }

   @Test
   @WithUserDetails("hanako.samurai@example.com")
   @Transactional
   public void 管理者としてログイン済みの場合は利用規約更新後に利用規約ページにリダイレクトする() throws Exception {
       mockMvc.perform(post("/admin/terms/update").with(csrf()).param("content", "テスト内容"))
              .andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/admin/terms"));

       Term term = termService.findFirstTermByOrderByIdDesc();
       assertThat(term.getContent()).isEqualTo("テスト内容");
   }
}