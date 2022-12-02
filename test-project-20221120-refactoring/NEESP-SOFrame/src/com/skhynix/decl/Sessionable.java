package com.skhynix.decl;

public interface Sessionable {
	public String openSession(String prefixKey, String jsonParams); 
	public boolean closeSession(String sessionKey);
	public void closeAllSession();
	public void testSession();
}
