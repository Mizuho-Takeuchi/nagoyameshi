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

import com.example.nagoyameshi.entity.Company;
import com.example.nagoyameshi.service.CompanyService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminCompanyControllerTest {
   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private CompanyService companyService;

   @Test
   public void 未ログインの場合は管理者用の会社概要ページからログインページにリダイレクトする() throws Exception {
       mockMvc.perform(get("/admin/company"))
              .andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/login"));
   }

   @Test
   @WithUserDetails("taro.samurai@example.com")
   public void 一般ユーザーとしてログイン済みの場合は管理者用の会社概要ページが表示されずに403エラーが発生する() throws Exception {
       mockMvc.perform(get("/admin/company"))
              .andExpect(status().isForbidden());
   }

   @Test
   @WithUserDetails("hanako.samurai@example.com")
   public void 管理者としてログイン済みの場合は管理者用の会社概要ページが正しく表示される() throws Exception {
       mockMvc.perform(get("/admin/company"))
              .andExpect(status().isOk())
              .andExpect(view().name("admin/company/index"));
   }

   @Test
   public void 未ログインの場合は管理者用の会社概要編集ページからログインページにリダイレクトする() throws Exception {
       mockMvc.perform(get("/admin/company/edit"))
              .andExpect(status().is3xxRedirection())
              .andExpect(redirectedUrl("/login"));
   }

   @Test
   @WithUserDetails("taro.samurai@example.com")
   public void 一般ユーザーとしてログイン済みの場合は管理者用の会社概要編集ページが表示されずに403エラーが発生する() throws Exception {
       mockMvc.perform(get("/admin/company/edit"))
              .andExpect(status().isForbidden());
   }

   @Test
   @WithUserDetails("hanako.samurai@example.com")
   public void 管理者としてログイン済みの場合は管理者用の会社概要編集ページが正しく表示される() throws Exception {
       mockMvc.perform(get("/admin/company/edit"))
              .andExpect(status().isOk())
              .andExpect(view().name("admin/company/edit"));
   }

   @Test
   @Transactional
   public void 未ログインの場合は会社概要を更新せずにログインページにリダイレクトする() throws Exception {
       mockMvc.perform(post("/admin/company/update")
               .with(csrf())
               .param("name", "テスト会社名")
               .param("postalCode", "0000000")
               .param("address", "テスト住所")
               .param("representative", "テスト代表者")
               .param("establishmentDate", "2000年1月1日")
               .param("capital", "5,000千円")
               .param("business", "テスト事業内容")
               .param("numberOfEmployees", "50名"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/login"));

       Company company = companyService.findFirstCompanyByOrderByIdDesc();
       assertThat(company.getName()).isEqualTo("NAGOYAMESHI株式会社");
       assertThat(company.getPostalCode()).isEqualTo("1010022");
       assertThat(company.getAddress()).isEqualTo("東京都千代田区神田練塀町300番地 住友不動産秋葉原駅前ビル5F");
       assertThat(company.getRepresentative()).isEqualTo("侍 太郎");
       assertThat(company.getEstablishmentDate()).isEqualTo("2015年3月19日");
       assertThat(company.getCapital()).isEqualTo("110,000千円");
       assertThat(company.getBusiness()).isEqualTo("飲食店等の情報提供サービス");
       assertThat(company.getNumberOfEmployees()).isEqualTo("83名");
   }

   @Test
   @WithUserDetails("taro.samurai@example.com")
   @Transactional
   public void 一般ユーザーとしてログイン済みの場合は会社概要を更新せずに403エラーが発生する() throws Exception {
       mockMvc.perform(post("/admin/company/update")
               .with(csrf())
               .param("name", "テスト会社名")
               .param("postalCode", "0000000")
               .param("address", "テスト住所")
               .param("representative", "テスト代表者")
               .param("establishmentDate", "2000年1月1日")
               .param("capital", "5,000千円")
               .param("business", "テスト事業内容")
               .param("numberOfEmployees", "50名"))
           .andExpect(status().isForbidden());

       Company company = companyService.findFirstCompanyByOrderByIdDesc();
       assertThat(company.getName()).isEqualTo("NAGOYAMESHI株式会社");
       assertThat(company.getPostalCode()).isEqualTo("1010022");
       assertThat(company.getAddress()).isEqualTo("東京都千代田区神田練塀町300番地 住友不動産秋葉原駅前ビル5F");
       assertThat(company.getRepresentative()).isEqualTo("侍 太郎");
       assertThat(company.getEstablishmentDate()).isEqualTo("2015年3月19日");
       assertThat(company.getCapital()).isEqualTo("110,000千円");
       assertThat(company.getBusiness()).isEqualTo("飲食店等の情報提供サービス");
       assertThat(company.getNumberOfEmployees()).isEqualTo("83名");
   }

   @Test
   @WithUserDetails("hanako.samurai@example.com")
   @Transactional
   public void 管理者としてログイン済みの場合は会社概要更新後に会社概要ページにリダイレクトする() throws Exception {
       mockMvc.perform(post("/admin/company/update")
               .with(csrf())
               .param("name", "テスト会社名")
               .param("postalCode", "0000000")
               .param("address", "テスト住所")
               .param("representative", "テスト代表者")
               .param("establishmentDate", "2000年1月1日")
               .param("capital", "5,000千円")
               .param("business", "テスト事業内容")
               .param("numberOfEmployees", "50名"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/admin/company"));

       Company company = companyService.findFirstCompanyByOrderByIdDesc();
       assertThat(company.getName()).isEqualTo("テスト会社名");
       assertThat(company.getPostalCode()).isEqualTo("0000000");
       assertThat(company.getAddress()).isEqualTo("テスト住所");
       assertThat(company.getRepresentative()).isEqualTo("テスト代表者");
       assertThat(company.getEstablishmentDate()).isEqualTo("2000年1月1日");
       assertThat(company.getCapital()).isEqualTo("5,000千円");
       assertThat(company.getBusiness()).isEqualTo("テスト事業内容");
       assertThat(company.getNumberOfEmployees()).isEqualTo("50名");
   }
}