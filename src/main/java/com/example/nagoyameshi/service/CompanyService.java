package com.example.nagoyameshi.service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Company;
import com.example.nagoyameshi.form.CompanyEditForm;
import com.example.nagoyameshi.repository.CompanyRepository;

@Service
public class CompanyService {
	private final CompanyRepository companyRepository;
	
	public CompanyService(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
	}
	
	public Company findFirstCompanyByOrderByIdDesc() {
		return companyRepository.findFirstByOrderByIdDesc();
	}
	
	@Transactional
	public void updateCompany(Company company, CompanyEditForm companyEditForm) {
		company.setName(companyEditForm.getName());
		company.setPostalCode(companyEditForm.getPostalCode());
		company.setAddress(companyEditForm.getAddress());
		company.setRepresentative(companyEditForm.getRepresentative());
		company.setEstablishmentDate(companyEditForm.getEstablishmentDate());
		company.setCapital(companyEditForm.getCapital());
		company.setBusiness(companyEditForm.getBusiness());
		company.setNumberOfEmployees(companyEditForm.getNumberOfEmployees());
		
		companyRepository.save(company);
	}
}
