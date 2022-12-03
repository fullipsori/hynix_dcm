package com.skhynix.controller;

import java.util.function.Consumer;
import java.util.function.Function;

import com.skhynix.common.StringUtil;
import com.skhynix.extern.BusinessBehavior;
import com.skhynix.manager.MetaDataManager;
import com.skhynix.model.factory.WaferData;

public class BusinessLogic implements BusinessBehavior {

	private static final BusinessLogic instance = new BusinessLogic();
	private volatile BusinessBehavior businessBehavior = null;
	private final MetaDataManager metaDataManager = MetaDataManager.getInstance();

	public BusinessLogic() { }
	
	public static BusinessLogic getInstance() {
		return instance;
	}

	public void setBusinessBehavior(BusinessBehavior businessBehavior) {
		this.businessBehavior = businessBehavior;
	}

	public Consumer<Object> resultConsumer = response -> {
		System.out.println("doBusiness Callback:" + (String)response);
	};

	@Override
	public String doBusiness(String eventType, String message, Function<Object, Object> metaSource, Consumer<Object> resultConsumer) throws Exception {
		// TODO Auto-generated method stub
		if(businessBehavior != null) {
			/* 테스트를 위해 WaferModel json 데이터로 변환하여 보낸다. */
			WaferData waferData = new WaferData().setWaferId("wafer-1919").setMessage(message).setMetadataKey(1).setSensorData("090909");
			String jsonString = StringUtil.objectToJson(waferData);
			return businessBehavior.doBusiness(eventType, jsonString, 
					(metaSource == null)? metaDataManager.defaultDataSourcer : metaSource, 
					(resultConsumer == null)? this.resultConsumer : resultConsumer);
		} else {
			return "no business Logic";
		}
	}
}
