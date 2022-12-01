package com.skhynix.decl;


public interface Attendable {

	public void attached(Runnable unregister);
	public void detached();
	
}
