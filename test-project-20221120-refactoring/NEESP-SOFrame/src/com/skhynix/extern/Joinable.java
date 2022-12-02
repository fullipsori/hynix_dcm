package com.skhynix.extern;


public interface Joinable {

	public void joined(Runnable unregister);
	public void disjoined();
	
}
