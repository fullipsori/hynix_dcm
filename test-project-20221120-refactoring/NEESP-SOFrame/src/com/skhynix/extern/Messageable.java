package com.skhynix.extern;

public interface Messageable {
	public boolean sendMessage(String sessionKey, String msg);
	public String receiveMessage(String sessionKey);
}
