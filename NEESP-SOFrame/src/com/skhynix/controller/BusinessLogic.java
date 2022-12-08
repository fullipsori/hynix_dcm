package com.skhynix.controller;

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
import com.skhynix.model.message.BaseMsgModel;
import com.skhynix.model.session.BaseSessModel;


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
		String resourceType = String.format("resource%sas", BaseSessModel.defaultDelimiter);

		if(businessBehavior != null) {
			/* 테스트를 위해 WaferModel json 데이터로 변환하여 보낸다. */
			String jsonString = StringUtil.objectToJson(waferData);
			return businessBehavior.doBusiness(eventType, jsonString,  handles,
					(supplier == null)? this : supplier, 
					(resultConsumer == null)? this.resultConsumer : resultConsumer);
		} else {
			// default logic
			return "no-default logic";
		}
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
	public String sendAndReceive(String handle, String message, Map<String,String> properties, String replyQueue, String selector, long waitTimeInMillis) {
		// TODO Auto-generated method stub
		return Optional.ofNullable(messageRouter.sendAndReceive(handle, message, properties, replyQueue, selector, waitTimeInMillis)).map(BaseMsgModel::toJson).orElse("");
	}
	
	@Override
	public boolean sendMessage(String handle, String message, Map<String, String> properties) {
		// TODO Auto-generated method stub
		return messageRouter.sendMessage(handle, message, properties);
	}

	@Override
	public String receiveMessage(String handle, long waitTimeInMillis) throws Exception {
		// TODO Auto-generated method stub
		return Optional.ofNullable(messageRouter.receiveMessage(handle, waitTimeInMillis)).map(BaseMsgModel::toJson).orElse("");
	}

	@Override
	public boolean createMeta(String sourcetype, String table, Object dtoObject) {
		// TODO Auto-generated method stub
		return metaDataManager.createMetaData(sourcetype, table, dtoObject);
	}

	@Override
	public boolean retrieveMeta(String sourcetype, String table, Pair<String, String> key, Object dtoObject) {
		// TODO Auto-generated method stub
		return metaDataManager.retrieveMeta(sourcetype, table, key, dtoObject);
	}

	@Override
	public boolean updateMeta(String sourcetype, String table, Object dtoObject) {
		// TODO Auto-generated method stub
		return metaDataManager.updateMeta(sourcetype, table, dtoObject);
	}

	@Override
	public boolean deleteMeta(String sourcetype, String table, Pair<String, ? extends Object> key) {
		// TODO Auto-generated method stub
		return metaDataManager.deleteMeta(sourcetype, table, key);
	}

	@Override
	public <E> List<E> executeSql(String sourcetype, Class<E> clazz, String sqlString) {
		// TODO Auto-generated method stub
		return executeSql(sourcetype, clazz, sqlString);
	}

}
