package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.RegularHoliday;
import com.example.nagoyameshi.entity.RegularHolidayRestaurant;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.RegularHolidayRestaurantRepository;

@Service
public class RegularHolidayRestaurantService {
   private final RegularHolidayRestaurantRepository regularHolidayRestaurantRepository;
   private final RegularHolidayService regularHolidayService;

   public RegularHolidayRestaurantService(RegularHolidayRestaurantRepository regularHolidayRestaurantRepository, RegularHolidayService regularHolidayService) {
       this.regularHolidayRestaurantRepository = regularHolidayRestaurantRepository;
       this.regularHolidayService = regularHolidayService;
   }

   // 指定した店舗の定休日のidをリスト形式で取得する
   public List<Integer> findRegularHolidayIdsByRestaurant(Restaurant restaurant) {
       return regularHolidayRestaurantRepository.findRegularHolidayIdsByRestaurant(restaurant);
   }

   @Transactional
   public void createRegularHolidaysRestaurants(List<Integer> regularHolidayIds, Restaurant restaurant) {
       for (Integer regularHolidayId : regularHolidayIds) {
           if (regularHolidayId != null) {
               Optional<RegularHoliday> optionalRegularHoliday = regularHolidayService.findRegularHolidayById(regularHolidayId);

               if (optionalRegularHoliday.isPresent()) {
                   RegularHoliday regularHoliday = optionalRegularHoliday.get();

                   Optional<RegularHolidayRestaurant> optionalCurrentRegularHolidayRestaurant = regularHolidayRestaurantRepository.findByRestaurantAndRegularHoliday(restaurant, regularHoliday);

                   // 重複するエンティティが存在しない場合は新たにエンティティを作成する
                   if (optionalCurrentRegularHolidayRestaurant.isEmpty()) {
                       RegularHolidayRestaurant regularHolidayRestaurant = new RegularHolidayRestaurant();
                       regularHolidayRestaurant.setRestaurant(restaurant);
                       regularHolidayRestaurant.setRegularHoliday(regularHoliday);

                       regularHolidayRestaurantRepository.save(regularHolidayRestaurant);
                   }
               }
           }
       }
   }

   @Transactional
   public void syncRegularHolidaysRestaurants(List<Integer> newRegularHolidayIds, Restaurant restaurant) {
       List<RegularHolidayRestaurant> currentRegularHolidaysRestaurants = regularHolidayRestaurantRepository.findByRestaurant(restaurant);

       if (newRegularHolidayIds == null) {
           // newRegularHolidayIdsがnullの場合はすべてのエンティティを削除する
           for (RegularHolidayRestaurant currentRegularHolidayRestaurant : currentRegularHolidaysRestaurants) {
               regularHolidayRestaurantRepository.delete(currentRegularHolidayRestaurant);
           }
       } else {
           // 既存のエンティティが新しいリストに存在しない場合は削除する
           for (RegularHolidayRestaurant currentRegularHolidayRestaurant : currentRegularHolidaysRestaurants) {
               if (!newRegularHolidayIds.contains(currentRegularHolidayRestaurant.getRegularHoliday().getId())) {
                   regularHolidayRestaurantRepository.delete(currentRegularHolidayRestaurant);
               }
           }

           for (Integer newRegularHolidayId : newRegularHolidayIds) {
               if (newRegularHolidayId != null) {
                   Optional<RegularHoliday> optionalRegularHoliday = regularHolidayService.findRegularHolidayById(newRegularHolidayId);

                   if (optionalRegularHoliday.isPresent()) {
                       RegularHoliday regularHoliday = optionalRegularHoliday.get();

                       Optional<RegularHolidayRestaurant> optionalCurrentRegularHolidayRestaurant = regularHolidayRestaurantRepository.findByRestaurantAndRegularHoliday(restaurant, regularHoliday);

                       // 重複するエンティティが存在しない場合は新たにエンティティを作成する
                       if (optionalCurrentRegularHolidayRestaurant.isEmpty()) {
                           RegularHolidayRestaurant regularHolidayRestaurant = new RegularHolidayRestaurant();
                           regularHolidayRestaurant.setRestaurant(restaurant);
                           regularHolidayRestaurant.setRegularHoliday(regularHoliday);

                           regularHolidayRestaurantRepository.save(regularHolidayRestaurant);
                       }
                   }
               }
           }
       }
   }
}