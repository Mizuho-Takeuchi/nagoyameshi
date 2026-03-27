package com.example.nagoyameshi.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import lombok.Data;

@Data
public class ReviewRegisterForm {
	@NotNull(message = "評価を選択してください。")
	@Range(min = 1, max = 5, message = "評価は1～5のいずれかを選択してください。")
	private Integer score;
	
	@NotBlank(message = "感想を入力してください。")
	@Length(max = 300, message = "感想は300文字以内で入力してください。")
	private String content;
}
