package com.skhynix.extern;

/**
 * 데이터를 주고받을때 Pair 형태로 주고받는 경우 사용되는 유틸 클래스 입니다.
 * @author fullipsori
 *
 * @param <F>
 * @param <S>
 */
public class Pair<F, S> {
	private F first;
	private S second;
	
	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}
	
	public void setFirst(F first) {
		this.first = first;
	}
	public void setSecond(S second) {
		this.second = second;
	}
	
	public F getFirst() {
		return first;
	}
	public S getSecond() {
		return second;
	}

}
