package com.skhynix.decl;


public interface Joinable {

	public void joined(Runnable unregister);
	public void disjoined();
	
}
