package com.example.nagoyameshi.service;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.User;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerCreateParams.Address;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.CustomerUpdateParams.InvoiceSettings;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.PaymentMethodDetachParams;
import com.stripe.param.SubscriptionCancelParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionListParams;



@Service
public class StripeService {    
    @Value("${stripe.api-key}")
    private String apiKey;
    
    //依存性の注入後、一度だけ実行するメソッド
    @PostConstruct
    private void init() {
    	Stripe.apiKey = apiKey;
    }
    
    //顧客（StripeのCustomerオブジェクト）を作成する。
    public Customer createCustomer(User user) throws StripeException {
    	Address address = Address.builder()
		    			.setLine1(user.getAddress())
		    			.setPostalCode(user.getPostalCode())    
		    			.build();
    			
    	CustomerCreateParams customerCreateParams = CustomerCreateParams.builder()
									    			.setAddress(address)
									    			.setEmail(user.getEmail())
									    			.setName(user.getName())
									    			.setPhone(user.getPhoneNumber()) 
									    			.build();
    	
    	return Customer.create(customerCreateParams);
    }
    
    //支払い方法（StripeのPaymentMethodオブジェクト）を顧客（StripeのCustomerオブジェクト）に紐づける。
    public void attachPaymentMethodToCustomer(String paymentMethodId, String customerId) throws StripeException{
    	PaymentMethodAttachParams paymentMethodAttachParams = PaymentMethodAttachParams.builder()
											    			.setCustomer(customerId)    
											    			.build();
    	PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
    	paymentMethod.attach(paymentMethodAttachParams);
    }
    
    //顧客（StripeのCustomerオブジェクト）のデフォルトの支払い方法（StripeのPaymentMethodオブジェクト）を設定する。
    public void setDefaultPaymentMethod(String paymentMethodId, String customerId) throws StripeException {
    	InvoiceSettings invoiceSettings = InvoiceSettings.builder()
						    			.setDefaultPaymentMethod(paymentMethodId)
						    			.build();
    	
    	CustomerUpdateParams customerUpdateParams = CustomerUpdateParams.builder()
									    			.setInvoiceSettings(invoiceSettings)
									    			.build();
    	Customer customer = Customer.retrieve(customerId);
    	customer.update(customerUpdateParams);
    }
    
    //サブスクリプション（StripeのSubscriptionオブジェクト）を作成する。
	public Subscription createSubscription(String priceId, String customerId) throws StripeException {
		SubscriptionCreateParams.Item item = SubscriptionCreateParams.Item.builder()
																			.setPrice(priceId)
																			.build();
		
		SubscriptionCreateParams subscriptionCreateParams = SubscriptionCreateParams.builder()
																					.setCustomer(customerId)
																					.addItem(item)
																					.build();
		return Subscription.create(subscriptionCreateParams);
	}
    
    //顧客（StripeのCustomerオブジェクト）のデフォルトの支払い方法（StripeのPaymentMethodオブジェクト）を取得する。
    public PaymentMethod getDefaultPaymentMethod(String customerId) throws StripeException {
    	Customer customer = Customer.retrieve(customerId);
    	String paymentMethod = customer.getInvoiceSettings().getDefaultPaymentMethod();
    	return PaymentMethod.retrieve(paymentMethod);
    }
    
    // 顧客（StripeのCustomerオブジェクト）のデフォルトの支払い方法（StripeのPaymentMethodオブジェクト）のIDを取得する
    public String getDefaultPaymentMethodId(String customerId) throws StripeException {
        Customer customer = Customer.retrieve(customerId);
        return customer.getInvoiceSettings().getDefaultPaymentMethod();
    }
    
    //支払い方法（StripeのPaymentMethodオブジェクト）と顧客（StripeのCustomerオブジェクト）の紐づけを解除する。
    public void detachPaymentMethodFromCustomer(String paymentId) throws StripeException {
    	PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentId);
    	paymentMethod.detach(PaymentMethodDetachParams.builder().build());
    }
    
    //サブスクリプション（StripeのSubscriptionオブジェクト）を取得する。
    public List<Subscription> getSubscriptions(String customerId) throws StripeException {
    	SubscriptionListParams subscriptionListParams = SubscriptionListParams.builder()
    			.setCustomer(customerId)
    			.build();
    	SubscriptionCollection subscriptionCollection = Subscription.list(subscriptionListParams);
    	return subscriptionCollection.getData();
    }
    
    //サブスクリプション（StripeのSubscriptionオブジェクト）をキャンセルする。
    public void cancelSubscriptions(List<Subscription> subscriptions) throws StripeException {
    	for(Subscription subscription : subscriptions) {
    		SubscriptionCancelParams subscriptionCancelParams = SubscriptionCancelParams.builder().build();
    		subscription.cancel(subscriptionCancelParams);
    	}
    }
}
