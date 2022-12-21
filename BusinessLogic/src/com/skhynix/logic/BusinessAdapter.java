package com.skhynix.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class BusinessAdapter {
	public static final String asResourceType = "resource-as";
	private static final BusinessAdapter instance = new BusinessAdapter();
	
	public static BusinessAdapter getInstance() {
		return instance;
	}

	/** 요청은 모든 String 타입으로 되어 있음으로 이를 기반으로 동작되도록 하며, multi- operation 이 필요한지 여부는 확인이 필요함. */
	/**
	 * 
	 * @param resourceType activespace 의 경우 "resource-as" 이다. 
	 * @param tableName  접속할 Table정보 
	 * @param dtoObject data Class Object
	 * @return
	 */
	public boolean createMeta(BiFunction<String,Object,Object> function, String resourceType, String tableName, Object dtoObject) {
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("resourceType", resourceType);
		requestParams.put("tableName", tableName);
		requestParams.put("dto", dtoObject);
		return (Boolean)Optional.ofNullable(function.apply("createMeta", requestParams)).orElse(Boolean.FALSE);
	}
	public boolean retrieveMeta(BiFunction<String,Object,Object> function, String resourceType, String tableName, Map<String,String> keyValue, Object dtoObject) {
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("resourceType", resourceType);
		requestParams.put("tableName", tableName);
		requestParams.put("keyValue", keyValue);
		requestParams.put("dto", dtoObject);
		return (Boolean)Optional.ofNullable(function.apply("retrieveMeta", requestParams)).orElse(Boolean.FALSE);
	}
	public boolean updateMeta(BiFunction<String,Object,Object> function, String resourceType, String tableName, Object dtoObject) {
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("resourceType", resourceType);
		requestParams.put("tableName", tableName);
		requestParams.put("dto", dtoObject);
		return (Boolean)Optional.ofNullable(function.apply("updateMeta", requestParams)).orElse(Boolean.FALSE);
	}
	public boolean deleteMeta(BiFunction<String,Object,Object> function, String resourceType, String tableName, Map<String,String> keyValue) {
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("resourceType", resourceType);
		requestParams.put("tableName", tableName);
		requestParams.put("keyValue", keyValue);
		return (Boolean)Optional.ofNullable(function.apply("deleteMeta", requestParams)).orElse(Boolean.FALSE);
		
	}
    @SuppressWarnings("unchecked")
	public <E> List<E> executeSql(BiFunction<String,Object,Object> function, String resourceType, Class<E> clazz, String sqlString) {
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("resourceType", resourceType);
		requestParams.put("clazz", clazz);
		requestParams.put("sqlString", sqlString);
		return (List<E>)Optional.ofNullable(function.apply("executeSql", requestParams)).orElse(null);
    }
	
	public void sendSyncMessage(BiFunction<String,Object,Object> function, Map<String,String> handles, String message) {
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("handles", handles);
		requestParams.put("message", message);
		function.apply("sendSyncMessage", requestParams);
	}
	public void sendAsyncMessage(BiFunction<String,Object,Object> function, Map<String,String> handles, String message) {
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("handles", handles);
		requestParams.put("message", message);
		function.apply("sendAsyncMessage", requestParams);
	}
	
	/**  
	 *  EMS 메세지의 경우만 하기 함수들이 지원되고 다른 messaging 모듈에서도 필요한 경우 추가 개발 필요함.
	 * @param handle send 용 handle
	 * @param param receive 용 parameter (queue name)
	 * @param message send message
	 * @return json String
	 */
	public boolean sendMessage(BiFunction<String,Object,Object> function, String handle, String message, Map<String,String> properties) {
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("handle", handle);
		requestParams.put("message", message);
		if(properties != null) requestParams.put("properties", properties);
		return (Boolean)Optional.ofNullable(function.apply("sendMessage", requestParams)).orElse(Boolean.FALSE);
	}
	/** 결과값은 json String 값입니다. key 중에 message 는 body 에 해당하고 나머지는 property 입니다.  **/
	public String receiveMessage(BiFunction<String,Object,Object> function, String handle, long waitTimeInMillis) {
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("handle", handle);
		requestParams.put("waitTimeInMillis", Long.toString(waitTimeInMillis));
		return (String)Optional.ofNullable(function.apply("receiveMessage", requestParams)).orElse(null);
	}
	public String sendAndReceive(BiFunction<String,Object,Object> function, String handle, String message, Map<String,String> properties, String replyQueue, String selector, long waitTimeInMillis) {
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("handle", handle);
		requestParams.put("message", message);
		if(properties != null) requestParams.put("properties", properties);
		if(replyQueue != null && !replyQueue.isEmpty()) requestParams.put("replyQueue", replyQueue);
		if(selector != null && !selector.isEmpty()) requestParams.put("selector", selector);
		requestParams.put("waitTimeInMillis", Long.toString(waitTimeInMillis));
		return (String)Optional.ofNullable(function.apply("sendAndReceive", requestParams)).orElse(null);
	}
	public <T,U> String mapToJson(BiFunction<String,Object,Object> function, Map<T,U> map) {
		return (String)function.apply("mapToJson", map);
	}
	@SuppressWarnings("unchecked")
	public <T,U> Map<T,U> jsonToMap(BiFunction<String,Object,Object> function, String jsonString) {
		return (Map<T, U>)function.apply("jsonToMap", jsonString);
	}

	public String objToJson(BiFunction<String,Object,Object> function, Object obj) {
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("object", obj);
		return (String)function.apply("ObjToJson", requestParams);
	}
	public Object jsonToObj(BiFunction<String,Object,Object> function, String jsonString, Class<?> clazz) {
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("jsonString", jsonString);
		requestParams.put("clazz", clazz);
		return function.apply("JsonToObj", requestParams);
	}
}
