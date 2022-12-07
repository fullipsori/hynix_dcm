package com.skhynix.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.skhynix.common.StringUtil;
import com.skhynix.extern.BusinessBehavior;
import com.skhynix.extern.BusinessSupplier;
import com.skhynix.extern.Pair;
import com.skhynix.extern.WaferData;
import com.skhynix.manager.MetaDataManager;
import com.skhynix.model.message.MessageModel;


public class BusinessLogic implements BusinessBehavior, BusinessSupplier {

	private static final BusinessLogic instance = new BusinessLogic();
	private volatile BusinessBehavior businessBehavior = null;
	private final MetaDataManager metaDataManager = MetaDataManager.getInstance();
	private static final MessageRouter messageRouter = MessageRouter.getInstance();

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
	public String doBusiness(String eventType, String message, Map<String,String> handles, BusinessSupplier supplier, Consumer<Object> resultConsumer) throws Exception {
		// TODO Auto-generated method stub
		//fullip
		WaferData waferData = new WaferData().setWaferId("wafer-1919").setMessage(message).setMetadataKey(1).setSensorData("090909");

		if(businessBehavior != null) {
			/* 테스트를 위해 WaferModel json 데이터로 변환하여 보낸다. */
			String jsonString = StringUtil.objectToJson(waferData);
			return businessBehavior.doBusiness(eventType, jsonString,  handles,
					(supplier == null)? this : supplier, 
					(resultConsumer == null)? this.resultConsumer : resultConsumer);
		} else {
			// default logic
			List<Pair<String, String>> params = new ArrayList<>();
			params.add(Pair.of("key", String.valueOf(waferData.metadataKey)));
			String meta = (String)requestMeta("resource,as", "my_table", params);
			if(StringUtil.isEmpty(meta)) {
				params.clear();
				params.add(Pair.of("key", String.valueOf(waferData.metadataKey)));
				params.add(Pair.of("value", "initial"));
				meta = (String)requestMeta("resource,as", "my_table", params);
			} else {
				meta = Instant.now().toString();
				params.clear();
				params.add(Pair.of("key", String.valueOf(waferData.metadataKey)));
				params.add(Pair.of("value", meta));
				boolean res = storeMeta("resource,as", "my_table", params);
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

	@Override
	public void sendSyncMessage(String[] handles, String message) {
		// TODO Auto-generated method stub
		messageRouter.sendSyncTo(handles, message);
	}

	@Override
	public void sendAsyncMessage(String[] handles, String message) {
		// TODO Auto-generated method stub
		messageRouter.sendAsyncTo(handles, message);
	}

	@Override
	public String sendAndReceive(String handle, String replyQueue, String selector, String message) {
		// TODO Auto-generated method stub
		return Optional.ofNullable(messageRouter.sendAndReceive(handle, replyQueue, selector, message)).map(MessageModel::toString).orElse("");
	}

}
