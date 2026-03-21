package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.StripeService;
import com.example.nagoyameshi.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;

@Controller
@RequestMapping("/subscription")
public class SubscriptionController {
	@Value("${stripe.premium-plan-price-id}")
	private String premiumPlanPriceId;
	
	private final StripeService stripeService;
	private final UserService userService;
	
	public SubscriptionController(StripeService stripeService,
								UserService userService) {
		this.stripeService = stripeService;
		this.userService = userService;
	}
	
	//有料プラン登録ページを表示する。
	@GetMapping("/register")
	public String register() {
		return "subscription/register";
	}
	
	//現在ログイン中のユーザーを顧客として作成し、
	//フォームから送信されたクレジットカード情報をデフォルトの支払い方法として設定する。
	//また、顧客のサブスクリプションを作成し、ロールを更新する。
	@PostMapping("/create")
	public String create(@RequestParam(name = "paymentMethodId")String paymentMethodId,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		String stripeCustomerId = user.getStripeCustomerId();
		
		if(stripeCustomerId == null) {
			try {
				Customer customer = stripeService.createCustomer(user);
				String customerId= customer.getId();
				userService.saveStripeCustomerId(user, customerId);
			}catch (StripeException e){
				redirectAttributes.addFlashAttribute("errorMessage", "有料プランへの登録に失敗しました。再度お試しください。");
				return "redirect:/";
			}
		}
		
		try {
			stripeService.attachPaymentMethodToCustomer(paymentMethodId, stripeCustomerId);
			stripeService.setDefaultPaymentMethod(paymentMethodId, stripeCustomerId);
			stripeService.createSubscription(premiumPlanPriceId,stripeCustomerId);
		}catch(StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "有料プランへの登録に失敗しました。再度お試しください。");
			return "redirect:/";
		}
		
		userService.updateRole(user, "ROLE_PAID_MEMBER");
        userService.refreshAuthenticationByRole("ROLE_PAID_MEMBER");
		
        redirectAttributes.addFlashAttribute("successMessage", "有料プランへの登録が完了しました。");
        return "redirect:/";
	}
	
	//お支払い方法編集ページを表示する。
	@GetMapping("/edit")
	public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						Model model,
						RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		String stripeCustomerId = user.getStripeCustomerId();
		
		try {
			PaymentMethod paymentMethod = stripeService.getDefaultPaymentMethod(stripeCustomerId);
			model.addAttribute("card", paymentMethod.getCard());
            model.addAttribute("cardHolderName", paymentMethod.getBillingDetails().getName());
		}catch(StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "お支払い方法を取得できませんでした。再度お試しください。");
            return "redirect:/";
		}
		
		return "subscription/edit";
	}
	
	//顧客のデフォルトの支払い方法を更新する。
	@PostMapping("/update")
	public String update(@RequestParam(name = "paymentMethodId")String paymentMethodId,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		String stripeCustomerId = user.getStripeCustomerId();
		
		try {
			String defaultPaymentMethodId = stripeService.getDefaultPaymentMethodId(stripeCustomerId);
			stripeService.attachPaymentMethodToCustomer(paymentMethodId, stripeCustomerId);
			stripeService.setDefaultPaymentMethod(paymentMethodId, stripeCustomerId);
			stripeService.detachPaymentMethodFromCustomer(defaultPaymentMethodId);
		}catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "お支払い方法の変更に失敗しました。再度お試しください。");
			return "redirect:/";
		}
		redirectAttributes.addFlashAttribute("successMessage", "お支払い方法を変更しました。");
		return "redirect:/";
	}
	
	//有料プラン解約ページを表示する。
	@GetMapping("/cancel")
	public String cancel() {
		return "subscription/cancel";
	}
	
	//顧客のサブスクリプションをキャンセルし、デフォルトの支払い方法と顧客の紐づけを解除する。
	//また、ロールを更新する。
	@PostMapping("/delete")
	public String delete(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		String stripeCustomerId = user.getStripeCustomerId();
		
		try {
			//サブスクリプションを取得してキャンセル
			List<Subscription> subscriptions = stripeService.getSubscriptions(stripeCustomerId);
			stripeService.cancelSubscriptions(subscriptions);
			
			//支払方法を取得して紐づけを解除
			String defaultPaymentMethodId = stripeService.getDefaultPaymentMethodId(stripeCustomerId);
			stripeService.detachPaymentMethodFromCustomer(defaultPaymentMethodId);
		}catch(StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage","有料プランの解約に失敗しました。再度お試しください。");
			return "redirect:/";
		}
		
		userService.updateRole(user, "ROLE_FREE_MEMBER");
        userService.refreshAuthenticationByRole("ROLE_FREE_MEMBER");
        redirectAttributes.addFlashAttribute("successMessage","有料プランを解約しました。");
		return "redirect:/";
	}
}
