package com.skhynix.extern;

/**
 * Loading 되는 Business Logic 과 Owner Module 간에 데이터를 주고 받기 위한 인터페이스로서 
 * 로직 구현상 필요한 기능들이 있다면 이의 함수 정의들을 추가해 주어야 한다.  
 * Owner Module 의 BusinessLigic.java 내의 하기 함수들은 모두 구현되어야 한다.
 * @author fullipsori
 *
 */
public interface BusinessSupplier {
	/* Meta 데이터를 가져오는 함수로서 다음의 순서로 데이터를 가져온다. 1. 힙메모리의 HashMap 2. ActiveSpace 테이블 */
	Object requestMeta(String sourceType, String table, Object params);
	/* Meta 데이터를 저장하기 위한 함수이다. 힙메모리와 ActiveSpace 에 모두 저장해야 할지는 business 정의에 따라 바뀐다. */
	boolean storeMeta(String sourceType, Object data, Object params);
	/* 제어를 요청하는 함수로서 용도는 현재 미정 */
	Object controlMeta(String sourceType, Object data, Object params);
	
	/* 메세지 라우팅 용도로 사용하는 함수이다. */
	void sendSyncMessage(String[] handles, String message);
	void sendAsyncMessage(String[] handles, String message);
	
	/** 메세지를 전송하고 응답을 받아야 하는 경우에 사용한다. 현재는 미구현 상태로 향후 개발예정 
	 * 
	 * @param handle send 용 handle
	 * @param param receive 용 parameter (queue name)
	 * @param message send message
	 * @return json String
	 */
	String sendAndReceive(String handle, String replyQueue, String selector, String message);
	
	/*
	 *  Edward Won = 2022/12/07   
	 *  
	 *      
	 *  
	 *  [수정해야 할 곳]
	 *  DoBusiness에서 사용하고 싶은 ASRepository에 새로운 메소드 queryDtoGeneric이 있는 경우
	 *  
	 *  extern
	 *  	Resourceable.java
	 *  
	 *  manager
	 *  	MetaDataManager.java
	 *  	ReousrceManager.java
	 *  
	 *  repository
	 *  	ASRepository.java
	 *  
	 *  - 추가 함수: AS Repository 확인 필요 (종속성 여부 확인 필요) 	 
	 * 
	 *  1) 키값이 여러 개이다. - Retrieve 수정 필요 (이런 형태로 넣을 경우 dto 객체에 대한 클래스가 비즈니스 로직쪽에만 선언되어 있어야 하는 것인지) -
	 *  
	 *     참조함수: getDto (String sourceType, Object dto, String tableName, Map<String, String> keyValues)
	 *  
	 *  2) Create를 수정할 필요가 있는지 확인 필요
	 *  
	 *     참조함수: putDto (String sourceType, Object dto, String tableName)
	 *  
	 *  3) SQL query를 통해서 여러 개의 Row를 가져오는 방법이 필요 
	 *     
	 *     - 이때 종속성을 피하기 위해서 어떻게 할 것인지 결정할 필요가 있다. - 현재는 사용해야 할 WaferState와 같이 Dto Class를 알아야 한다.	   
	 *     참조함수: <E> List<E> queryDtoGeneric(Class<E> clazz, String sqlString)
	 *  
	 */
}
