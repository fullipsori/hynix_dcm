package com.skhynix.extern;

import java.util.List;
import java.util.Map;

/**
 * Loading 되는 Business Logic 과 Owner Module 간에 데이터를 주고 받기 위한 인터페이스로서 
 * 로직 구현상 필요한 기능들이 있다면 이의 함수 정의들을 추가해 주어야 한다.  
 * Owner Module 의 BusinessLigic.java 내의 하기 함수들은 모두 구현되어야 한다.
 * @author fullipsori
 *
 */
public interface BusinessSupplier {
	/** 요청은 모든 String 타입으로 되어 있음으로 이를 기반으로 동작되도록 하며, multi- operation 이 필요한지 여부는 확인이 필요함. */
	/**
	 * 
	 * @param sourcetype activespace 의 경우 "resource-as" 이다. 
	 * @param table  접속할 Table정보 
	 * @param dtoObject data Class Object
	 * @return
	 */
	public boolean createMeta(String sourcetype, String table, Object dtoObject);
	public boolean retrieveMeta(String sourcetype, String table, Pair<String,String> key, Object dtoObject);
	public boolean updateMeta(String sourcetype, String table, Object dtoObject);
	public boolean deleteMeta(String sourcetype, String table, Pair<String,? extends Object> key);
    public <E> List<E> executeSql(String sourcetype, Class<E> clazz, String sqlString);
	
	/* 메세지를 여러 ep 로 보내기 위한 용도로 사용하는 함수이다. */
	public void sendSyncMessage(String[] handles, String message);
	public void sendAsyncMessage(String[] handles, String message);
	
	/**  
	 *  EMS 메세지의 경우만 하기 함수들이 지원되고 다른 messaging 모듈에서도 필요한 경우 추가 개발 필요함.
	 * @param handle send 용 handle
	 * @param param receive 용 parameter (queue name)
	 * @param message send message
	 * @return json String
	 */
	public boolean sendMessage(String handle, String message, Map<String,String> properties);
	/** 결과값은 json String 값입니다. key 중에 message 는 body 에 해당하고 나머지는 property 입니다.  **/
	public String receiveMessage(String handle, long waitTimeInMillis) throws Exception;
	public String sendAndReceive(String handle, String message, Map<String,String> properties, String replyQueue, String selector, long waitTimeInMillis);
	
}
