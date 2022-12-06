package com.skhynix.extern;

/**
 * Manager 클래스들이 상속받는 BaseManager 추상클래스는 Joinable 인터페이스를 구현한 클래스들이 등록이 되는 경우
 * 내부적으로 client 들을 자동 관리하고 join 이 완료되었을때와 탈퇴되었을때 하기 함수들을 자동호출합니다. 
 * @author fullipsori
 *
 */
public interface Joinable {

	public void joined(Runnable unregister);
	public void disjoined();
	
}
