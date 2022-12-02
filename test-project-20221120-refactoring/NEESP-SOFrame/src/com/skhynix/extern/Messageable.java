package com.skhynix.extern;

public interface Messageable {
	public boolean sendMessage(String handle, String msg);
	public String receiveMessage(String handle);
	public void confirmMessage(String handle);
}
