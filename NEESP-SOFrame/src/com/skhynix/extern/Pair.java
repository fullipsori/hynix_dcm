package com.skhynix.extern;

/**
 * 데이터를 주고받을때 Pair 형태로 주고받는 경우 사용되는 유틸 클래스 입니다.
 * @author fullipsori
 *
 * @param <F>
 * @param <S>
 */
public class Pair<F, S> {
	private final F first;
	private final S second;
	
	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}
	
	public F getFirst() {
		return first;
	}
	public S getSecond() {
		return second;
	}

    @Override
    // Checks specified object is "equal to" the current object or not
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
 
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
 
        Pair<?, ?> pair = (Pair<?, ?>) o;
 
        // call `equals()` method of the underlying objects
        if (!first.equals(pair.first)) {
            return false;
        }
        return second.equals(pair.second);
    }
 
    @Override
    // Computes hash code for an object to support hash tables
    public int hashCode()
    {
        // use hash codes of the underlying objects
        return 31 * first.hashCode() + second.hashCode();
    }
 
    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
 
    // Factory method for creating a typed Pair immutable instance
    public static <F, S> Pair <F, S> of(F f, S s)
    {
        // calls private constructor
        return new Pair<>(f, s);
    }
}
