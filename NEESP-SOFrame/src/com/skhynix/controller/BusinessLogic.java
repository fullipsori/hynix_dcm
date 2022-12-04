package com.skhynix.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.skhynix.common.StringUtil;
import com.skhynix.extern.BusinessBehavior;
import com.skhynix.extern.MetaFunction;
import com.skhynix.extern.Pair;
import com.skhynix.manager.MetaDataManager;
import com.skhynix.model.factory.WaferData;

public class BusinessLogic implements BusinessBehavior, MetaFunction {

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
	public String doBusiness(String eventType, String message, MetaFunction function, Consumer<Object> resultConsumer) throws Exception {
		// TODO Auto-generated method stub
		//fullip
		WaferData waferData = new WaferData().setWaferId("wafer-1919").setMessage(message).setMetadataKey(1).setSensorData("090909");

		if(businessBehavior != null) {
			/* 테스트를 위해 WaferModel json 데이터로 변환하여 보낸다. */
			String jsonString = StringUtil.objectToJson(waferData);
			return businessBehavior.doBusiness(eventType, jsonString, 
					(function == null)? this : function, 
					(resultConsumer == null)? this.resultConsumer : resultConsumer);
		} else {
			// default logic
			List<Pair<String, String>> params = new ArrayList<>();
			params.add(new Pair<String, String>("key", String.valueOf(waferData.metadataKey)));
			String meta = (String)requestMeta("resource:as", "my_grid", params);
			if(StringUtil.isEmpty(meta)) {
				params.clear();
				params.add(new Pair<String, String>("key", String.valueOf(waferData.metadataKey)));
				params.add(new Pair<String, String>("value", "initial"));
				meta = (String)requestMeta("resource:as", "my_grid", params);
			} else {
				meta = Instant.now().toString();
				params.clear();
				params.add(new Pair<String, String>("key", String.valueOf(waferData.metadataKey)));
				params.add(new Pair<String, String>("value", meta));
				boolean res = storeMeta("resource:as", "my_grid", params);
				if(!res) return "fail store";
			}
			return (StringUtil.isEmpty(meta)) ? "fail meta" : meta;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object requestMeta(String sourceType, String table, Object params) {
		// TODO Auto-generated method stub
		List<Pair<String, String>> paramList = (List<Pair<String, String>>) params;
		return (String)metaDataManager.getMetaData(sourceType, (String)table, paramList.get(0).getFirst(), Long.valueOf(paramList.get(0).getSecond()));
	}

	@Override
	public Object controlMeta(String sourceType, Object data, Object params) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean storeMeta(String sourceType, Object data, Object params) {
		// TODO Auto-generated method stub
		List<Pair<String, String>> paramList = (List<Pair<String, String>>) params;
		return metaDataManager.putMetaData(sourceType, (String)data, paramList.get(0).getFirst(), Long.valueOf(paramList.get(0).getSecond()), paramList.get(1).getFirst(), paramList.get(1).getSecond());
	}

}
