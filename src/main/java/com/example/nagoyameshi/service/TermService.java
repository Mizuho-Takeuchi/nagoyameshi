package com.example.nagoyameshi.service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Term;
import com.example.nagoyameshi.form.TermEditForm;
import com.example.nagoyameshi.repository.TermRepository;

@Service
public class TermService {
	private final TermRepository termRepository;
	
	public TermService(TermRepository termRepository) {
		this.termRepository = termRepository;
	}
	
	public Term findFirstTermByOrderByIdDesc() {
		return termRepository.findFirstByOrderByIdDesc();
	}
	
	@Transactional
	public void updateTerm(Term term, TermEditForm termEditForm) {
		term.setContent(termEditForm.getContent());
		termRepository.save(term);
	}
}
