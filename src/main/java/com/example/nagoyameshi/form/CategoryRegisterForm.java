package com.example.nagoyameshi.form;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class CategoryRegisterForm {
	@NotBlank(message = "カテゴリを入力してください。")
	private String name;
	
	private List<Integer> categoryIds;
}
