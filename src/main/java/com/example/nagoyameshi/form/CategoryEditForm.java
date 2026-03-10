package com.example.nagoyameshi.form;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryEditForm {
	@NotBlank(message = "カテゴリを入力してください。")
	private String name;
	
	private List<Integer> categoryIds;
}
