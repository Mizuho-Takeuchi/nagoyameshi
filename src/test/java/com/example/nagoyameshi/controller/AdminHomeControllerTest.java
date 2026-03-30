package com.example.nagoyameshi.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminHomeControllerTest {
	@Autowired
	private MockMvc mockMvc;
	
	@Test
	public void 未ログインの場合は管理者用のトップページからログインページにリダイレクトする() throws Exception{
		mockMvc.perform(get("/admin"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 一般ユーザーとしてログイン済みの場合は管理者用のトップページが表示されずに403エラーが発生する() throws Exception{
		mockMvc.perform(get("/admin"))
				.andExpect(status().isForbidden());
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合は管理者用のトップページが正しく表示される() throws Exception{
		mockMvc.perform(get("/admin"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/index"));
		}
}
