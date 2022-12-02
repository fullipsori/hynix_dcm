package com.skhynix.controller;

import java.util.function.Consumer;
import java.util.function.Function;

import com.skhynix.extern.BusinessBehavior;
import com.skhynix.manager.MetaDataManager;

/**
 * Proxy 에서 콜하는 Logic Class 이다.
 * @author fullipsori
 *
 */
public class BusinessLogic implements BusinessBehavior {

	private static final BusinessLogic instance = new BusinessLogic();
	private volatile BusinessBehavior businessBehavior = null;
	private final MetaDataManager metaDataManager = MetaDataManager.getInstance();

	public BusinessLogic() { }
	
	public static BusinessLogic getInstance() {
		return instance;
	}

	public void setBusinessBehavior(BusinessBehavior businessBehavior) {
		System.out.println("fullip: set BusinessManager");
		this.businessBehavior = businessBehavior;
	}

	public Consumer<Object> resultConsumer = result -> {
		System.out.println("result:" + (String)result);
	};

	@Override
	public String doBusiness(String eventType, String message, Function<Object, Object> metaSource, Consumer<Object> resultConsumer) throws Exception {
		// TODO Auto-generated method stub
		if(businessBehavior != null) {
			String result = businessBehavior.doBusiness(eventType, message, 
					(metaSource == null)? metaDataManager.defaultDataSourcer : metaSource, 
					(resultConsumer == null)? this.resultConsumer : resultConsumer);
			System.out.println("ok loaded logic: " + result);
			return result;
		} else {
			System.out.println("no business Logic");
			return "no business Logic";
		}
	}
}
