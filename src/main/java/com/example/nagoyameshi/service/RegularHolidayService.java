package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.RegularHoliday;
import com.example.nagoyameshi.repository.RegularHolidayRepository;

@Service
public class RegularHolidayService {
	private final RegularHolidayRepository regularHolidayRepository;
	
	public RegularHolidayService(RegularHolidayRepository regularHolidayRepository) {
		this.regularHolidayRepository = regularHolidayRepository;
	}
	
	public Optional<RegularHoliday> findRegularHolidayById(Integer id) {
		return regularHolidayRepository.findById(id);
	}
	
	public List<RegularHoliday> findAllRegularHolidays(){
		return regularHolidayRepository.findAll();
	}
}
