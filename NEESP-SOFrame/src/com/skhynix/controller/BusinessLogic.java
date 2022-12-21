package com.skhynix.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.skhynix.common.StringUtil;
import com.skhynix.extern.BusinessBehavior;
import com.skhynix.extern.BusinessSupplier;
import com.skhynix.extern.WaferData;
import com.skhynix.manager.MetaDataManager;
import com.skhynix.model.message.BaseMsgModel;
import com.skhynix.model.session.BaseSessModel;


public class BusinessLogic implements BusinessBehavior, BiFunction<String, Object, Object> {

	private static final BusinessLogic instance = new BusinessLogic();
	private volatile BiFunction<Object, Object, Object> businessBehavior = null;
	private static final MetaDataManager metaDataManager = MetaDataManager.getInstance();
	private static final MessageRouter messageRouter = MessageRouter.getInstance();

	public BusinessLogic() { }
	
	public static BusinessLogic getInstance() {
		return instance;
	}

	public void setBusinessBehavior(BiFunction<Object, Object, Object> businessBehavior) {
		this.businessBehavior = businessBehavior;
	}

	public Consumer<Object> resultConsumer = response -> {
		System.out.println("doBusiness Callback:" + (String)response);
	};

	@Override
	public String doBusiness(String eventType, String message, Map<String,String> handles) throws Exception {
		// TODO Auto-generated method stub
		//fullip
		WaferData waferData = new WaferData().setWaferId("wafer-1919").setMessage(message).setMetadataKey(1).setSensorData("090909");

		if(businessBehavior != null) {
			/* 테스트를 위해 WaferModel json 데이터로 변환하여 보낸다. */
			String jsonString = StringUtil.objectToJson(waferData);
			Map<String,Object> received = new HashMap<String,Object>();
			received.put("eventType", eventType);
			received.put("message", jsonString);
			received.put("handles", handles);

			return (String)businessBehavior.apply(received, this);
		} else {
			return "no-default logic";
		}
	}
	
	@Override
	public Object apply(String command, Object params) {
		// TODO Auto-generated method stub
		try {
			return OPERATION.applyOperation(command, params);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static enum OPERATION {
		SEND_SYNC_MESSAGE("sendSyncMessage"){
			@Override
			public Object apply(Object requestParams) throws Exception {
				// TODO Auto-generated method stub
				@SuppressWarnings("unchecked")
				Map<String, Object> params = (Map<String, Object>) requestParams;
				messageRouter.sendSyncTo((String[])params.get("handle"), (String)params.get("message"));
				return null;
			}
		},
		SEND_ASYNC_MESSAGE("sendAsyncMessage") {
			@Override
			public Object apply(Object requestParams) throws Exception {
				// TODO Auto-generated method stub
				@SuppressWarnings("unchecked")
				Map<String, Object> params = (Map<String, Object>) requestParams;
				messageRouter.sendAsyncTo((String[])params.get("handle"), (String)params.get("message"));
				return null;
			}
		},

		SEND_AND_RECEIVE("sendAndReceive") {
			@SuppressWarnings("unchecked")
			@Override
			public Object apply(Object requestParams) throws Exception {
				// TODO Auto-generated method stub
				Map<String, Object> params = (Map<String, Object>) requestParams;
				return Optional.ofNullable(messageRouter.sendAndReceive(
							(String)params.get("handle"), 
							(String)params.get("message"), 
							(Map<String,String>)params.get("properties"), 
							(String)params.get("replyQueue"), 
							(String)params.get("selector"), 
							(long)Optional.ofNullable(params.get("waitTimeInMillis")).map(time -> Long.parseLong((String)time)).orElse(0L)))
						.map(BaseMsgModel::toJson).orElse("");
			}
		},

		SEND_MESSAGE("sendMessage") {
			@SuppressWarnings("unchecked")
			@Override
			public Object apply(Object requestParams) throws Exception {
				// TODO Auto-generated method stub
				Map<String, Object> params = (Map<String, Object>) requestParams;
				// TODO Auto-generated method stub
				return messageRouter.sendMessage(
						(String)params.get("handle"), 
						(String)params.get("message"), 
						(Map<String,String>)params.get("properties"));
			}
		},

		RECEIVE_MESSAGE("receiveMessage") {
			@Override
			public Object apply(Object requestParams) throws Exception {
				// TODO Auto-generated method stub
				@SuppressWarnings("unchecked")
				Map<String, Object> params = (Map<String, Object>) requestParams;
				// TODO Auto-generated method stub
				return Optional.ofNullable(messageRouter.receiveMessage(
							(String)params.get("handle"),
							(long)Optional.ofNullable(params.get("waitTimeInMillis")).map(time -> Long.parseLong((String)time)).orElse(0L)))
						.map(BaseMsgModel::toJson).orElse("");
			}
		},
		CREATE_META("createMeta") {
			@Override
			public Object apply(Object requestParams) throws Exception {
				// TODO Auto-generated method stub
				@SuppressWarnings("unchecked")
				Map<String, Object> params = (Map<String, Object>) requestParams;
				// TODO Auto-generated method stub
				return metaDataManager.createMeta(
						(String)params.get("resourceType"), 
						(String)params.get("tableName"), 
						(Object)params.get("dto"));
			}
		},
		RETRIEVE_META("retrieveMeta") {
			@SuppressWarnings("unchecked")
			@Override
			public Object apply(Object requestParams) throws Exception {
				// TODO Auto-generated method stub
				Map<String, Object> params = (Map<String, Object>) requestParams;
				// TODO Auto-generated method stub
				return metaDataManager.retrieveMeta(
						(String)params.get("resourceType"), 
						(String)params.get("tableName"), 
						(Map<String,String>)params.get("keyValue"), 
						(Object)params.get("dto"));
			}
		},
		UPDATE_META("updateMeta") {
			@Override
			public Object apply(Object requestParams) throws Exception {
				// TODO Auto-generated method stub
				@SuppressWarnings("unchecked")
				Map<String, Object> params = (Map<String, Object>) requestParams;
				// TODO Auto-generated method stub
				return metaDataManager.updateMeta(
						(String)params.get("resourceType"), 
						(String)params.get("tableName"), 
						(Object)params.get("dto"));
			}
		},
		DELETE_META("deleteMeta") {
			@SuppressWarnings("unchecked")
			@Override
			public Object apply(Object requestParams) throws Exception {
				// TODO Auto-generated method stub
				Map<String, Object> params = (Map<String, Object>) requestParams;
				// TODO Auto-generated method stub
				return metaDataManager.deleteMeta(
						(String)params.get("resourceType"), 
						(String)params.get("tableName"), 
						(Map<String,String>)params.get("keyValue"));
			}
		},
		EXECUTE_SQL("executeSql") {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Object apply(Object requestParams) throws Exception {
				// TODO Auto-generated method stub
				Map<String, Object> params = (Map<String, Object>) requestParams;
				// TODO Auto-generated method stub
				return metaDataManager.executeSql(
						(String)params.get("resourceType"), 
						(Class)params.get("clazz"), 
						(String)params.get("sqlString")); 
			}
		},
		JSON_TO_MAP("jsonToMap") {
			@Override
			public Object apply(Object requestParams) throws Exception {
				// TODO Auto-generated method stub
				return StringUtil.jsonToObject((String)requestParams, Map.class);
			}
		},
		MAP_TO_JSON("mapToJson") {
			@Override
			public Object apply(Object requestParams) throws Exception {
				// TODO Auto-generated method stub
				if(!Map.class.isInstance(requestParams)) return null;
				return StringUtil.objectToJson(requestParams);
			}
		},
		JSON_TO_OBJ("JsonToObj") {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Object apply(Object requestParams) throws Exception {
				if(!Map.class.isInstance(requestParams)) return null;
				Map<String, Object> params = (Map<String, Object>) requestParams;
				return StringUtil.jsonToObject(
						(String)params.get("jsonString"), 
						(Class)params.get("clazz"));
			}
		},
		OBJ_TO_JSON("ObjToJson") {
			@SuppressWarnings({ "unchecked" })
			@Override
			public Object apply(Object requestParams) throws Exception {
				if(!Map.class.isInstance(requestParams)) return null;
				Map<String, Object> params = (Map<String, Object>) requestParams;
				return StringUtil.objectToJson(params.get("object"));
			}
		}
		;
		

		public final String opType;
		private OPERATION(String opType) {
			this.opType = opType;
		}
		public abstract Object apply(Object requestParams) throws Exception;
		
		public static Object applyOperation(String command, Object requestParams) {
			return Arrays.stream(values()).filter(value -> command.equals(value.opType)).findFirst().map(op -> {
				try {
					return op.apply(requestParams);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}).orElse(null);
		}
	}
}
