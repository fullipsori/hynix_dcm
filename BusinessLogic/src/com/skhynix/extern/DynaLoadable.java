package com.skhynix.extern;

/**
 *  동적로딩하는 클래스는 구현하여야 하며, 초기 로딩시 필요한 항목이 있다면 구현하십니다.
 *  DynaClassManager 가 하기 함수를 자동 호출합니다.
 * @author fullipsori
 *
 */
public interface DynaLoadable {
	public void loadClass();
	public void unloadClass();
}
