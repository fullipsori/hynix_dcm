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
	
	/* 메세지를 전송하고 응답을 받아야 하는 경우에 사용한다. 현재는 미구현 상태로 향후 개발예정 */
	String sendAndReceive(String handle, String message);
}
