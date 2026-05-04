package com.example.nagoyameshi.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.repository.RoleRepository;
import com.example.nagoyameshi.repository.UserRepository;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	
	public UserService(UserRepository userRepository, 
						RoleRepository roleRepository,
						PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Transactional
	public User createUser(SignupForm signupForm) {
		User user = new User();
		Role role = roleRepository.findByName("ROLE_FREE_MEMBER");
		
		user.setName(signupForm.getName());
        user.setFurigana(signupForm.getFurigana());
        user.setPostalCode(signupForm.getPostalCode());
        user.setAddress(signupForm.getAddress());
        user.setPhoneNumber(signupForm.getPhoneNumber());
        
        //誕生日（任意項目）
        if(signupForm.getBirthday().isEmpty()) {
        	user.setBirthday(null);
        }else {
        	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        	LocalDate birthday = LocalDate.parse(signupForm.getBirthday(), formatter);
        	user.setBirthday(birthday);
        }
        
        //職業（任意項目）
        if(signupForm.getOccupation().isEmpty()) {
        	user.setOccupation(null);
        }else {
        	user.setOccupation(signupForm.getOccupation());
        }
        
        user.setEmail(signupForm.getEmail());
        user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
        user.setRole(role);
        user.setEnabled(false);
		
		return userRepository.save(user);
	}
	
	public boolean isEmailRegistered(String email) {
		User user = userRepository.findByEmail(email);
		return user != null;
	}
	
	public boolean isSamePassword(String password, String passwordConfirmation) {
		return password.equals(passwordConfirmation);
	}
	
	@Transactional
	public void enableUser(User user) {
		user.setEnabled(true);
		userRepository.save(user);
	}
	
	//すべてのユーザーをページングされた状態で取得する。
	public Page<User> findAllUsers(Pageable pageable){
		return userRepository.findAll(pageable);
	}
	
	//指定されたキーワードを氏名またはフリガナに含むユーザーを、ページングされた状態で取得する。
	public Page<User> findUsersByNameLikeOrFuriganaLike(String nameKeyword, String FuriganaKeyword, Pageable pageable){
		return userRepository.findByNameLikeOrFuriganaLike("%"+nameKeyword+"%", "%"+FuriganaKeyword+"%", pageable);
	}
	
	//指定したidを持つユーザーを取得する。
	public Optional<User> findUserById(Integer id) {
		return userRepository.findById(id);
	}
	
	//フォームから送信された会員情報でデータベースを更新する。
    @Transactional
    public void updateUser(UserEditForm userEditForm, User user) {
        user.setName(userEditForm.getName());
        user.setFurigana(userEditForm.getFurigana());
        user.setPostalCode(userEditForm.getPostalCode());
        user.setAddress(userEditForm.getAddress());
        user.setPhoneNumber(userEditForm.getPhoneNumber());

        if (!userEditForm.getBirthday().isEmpty()) {
            user.setBirthday(LocalDate.parse(userEditForm.getBirthday(), DateTimeFormatter.ofPattern("yyyyMMdd")));
        } else {
            user.setBirthday(null);
        }

        if (!userEditForm.getOccupation().isEmpty()) {
            user.setOccupation(userEditForm.getOccupation());
        } else {
            user.setOccupation(null);
        }

        user.setEmail(userEditForm.getEmail());

        userRepository.save(user);
    }  
	
	//メールアドレスが変更されたかどうかをチェックする。
	public boolean isEmailChanged(UserEditForm userEditForm, User user) {
        return !userEditForm.getEmail().equals(user.getEmail());
    }
	
	//指定したメールアドレスを持つユーザーを取得する。
	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}
	
	@Transactional
	public void saveStripeCustomerId(User user, String custormerId) {
		user.setStripeCustomerId(custormerId);
		userRepository.save(user);
	}
	
	@Transactional
	public void updateRole(User user, String roleName) {
		Role role = roleRepository.findByName(roleName);
		user.setRole(role);
		userRepository.save(user);
	}
	
	// 認証情報のロールを更新する
    public void refreshAuthenticationByRole(String newRole) {
        // 現在の認証情報を取得する
        Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();

        // 新しい認証情報を作成する
        List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
        simpleGrantedAuthorities.add(new SimpleGrantedAuthority(newRole));
        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(currentAuthentication.getPrincipal(), currentAuthentication.getCredentials(), simpleGrantedAuthorities);

        // 認証情報を更新する
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
    }
    
    public long countUsersByRole_Name(String roleName) {
    	return userRepository.countByRole_Name(roleName);
    }
    
    //今日の日付が、誕生日の前後15日以内に入っているかどうかチェック
    public boolean isWithinBirthdayPeriod(User user, LocalDate today) {
    	LocalDate birthday = user.getBirthday();
    	int thisYear = today.getYear();
    	
    	//今年と去年と来年の誕生日を生成(1月と12月の年跨ぎを考えて)
    	for(int i = -1; i <= 1; i++) {
    		int targetYear = thisYear + i;
    		LocalDate targetBirthday = birthday.withYear(targetYear);
    		
    		LocalDate startDate = targetBirthday.minusDays(15);
            LocalDate endDate = targetBirthday.plusDays(15);
            
            if(!today.isBefore(startDate) && !today.isAfter(endDate)) {
            	return true;
            }
    	}
    	
    	return false;
    }
    
    //特定のロールのユーザー一覧をページングした状態で取得
    public Page<User> findUserByRole_Name(String roleName, Pageable pageable){
    	return userRepository.findByRole_Name(roleName, pageable);
    }
    
    //ログイン5回連続(30分以内)失敗時に、usersテーブルに30分後の日時を記録
   public void lockUser(String email) {
	   User user = userRepository.findByEmail(email);
	   
	   if(user != null) {
		 LocalDateTime now = LocalDateTime.now();
		 int faildAttempt = user.getFailedAttempt();

		 if(faildAttempt == 5) {
			 user.setLockedUntil(Timestamp.valueOf(now.plusMinutes(30)));
			 userRepository.save(user);
		 }else {
		 
			 if(user.getLastFailedAt() != null){
				 Timestamp lastFailed = user.getLastFailedAt();
				 long lastFaild = ChronoUnit.MINUTES.between(lastFailed.toLocalDateTime(), now);
				 
				 if(lastFaild >= 30) {
					 faildAttempt = 1;
				 }else {
					 faildAttempt = faildAttempt + 1;
				 }
			 }else{
				 faildAttempt = 1;
			 }
			 
			 user.setFailedAttempt(faildAttempt);
			 user.setLastFailedAt(Timestamp.valueOf(now));	
			 userRepository.save(user);
			 
			 if(user.getFailedAttempt() == 5) {
				 user.setLockedUntil(Timestamp.valueOf(now.plusMinutes(30)));
				 userRepository.save(user);
			 }
		 }
	   }
   }
   
   //ログイン成功時に、usersテーブルのfailed_attemptに0にする
   public void setFailedAttemptZero(String email) {
	   User user = userRepository.findByEmail(email);
	   if(user != null) {
		   user.setFailedAttempt(0);
		   userRepository.save(user);
	   }
   }
}
