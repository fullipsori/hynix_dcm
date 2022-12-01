package com.skhynix.decl;

public interface Messageable {
	public boolean sendMessage(String sessionKey, String msg);
	public String receivedMessage(String sessionKey);
}
