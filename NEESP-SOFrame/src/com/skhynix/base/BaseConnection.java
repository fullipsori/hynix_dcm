package com.skhynix.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.skhynix.extern.Joinable;
import com.skhynix.extern.SessionBehavior;
import com.skhynix.model.session.BaseSessModel;

abstract public class BaseConnection implements SessionBehavior, Joinable {

	public static final String defaultDelimiter = ",";

	protected Runnable unregister = null;

	protected BaseSessModel serverModel = null;
	protected Map<String, BaseSessModel> sessionMap = new ConcurrentHashMap<>();
	protected String connectionInfo;
	
	abstract public String getDefaultServerUrl();
	
	public String getConnectionInfo() {
		return connectionInfo;
	}

	protected void initConnectable(String domain, String jsonParams) {
		serverModel = makeSessModel(domain, jsonParams);
		connectServer(serverModel);
	}
	
	protected void deInitConnectable() {
		
		disconnectServer();
		serverModel = null;
	}

	private String getRandomKey() {
		return String.valueOf(ThreadLocalRandom.current().nextInt(100000));
	}
	
	public abstract String tokenizeSessionName(String prefixHandle);

	protected Map<String,String> tokenizeSessionKey(String handle) {
		Map<String,String> tokenMap = new HashMap<>();
		int lastidx = handle.lastIndexOf(defaultDelimiter);
		tokenMap.put("randomKey", handle.substring(lastidx + 1));
		String sessionName = tokenMap.put("sessionName", tokenizeSessionName(handle.substring(0, lastidx)));
		tokenMap.put("domain", handle.substring(0, handle.indexOf(sessionName)-1));
		return tokenMap;
	}

	private String getSessionKey(String domain, String sessionName) {
		return String.format("%s%s%s%s%s", domain, defaultDelimiter, sessionName, defaultDelimiter, getRandomKey());
	}
	
	@Override
	public String openSession(String domain, String serverUrl, String jsonParams) {

		if(serverModel == null) {
			initConnectable(domain, jsonParams);
		}

		BaseSessModel client = makeSessModel(domain, jsonParams);

		try {
			client = connectSession(client);
			if(client != null) {
				String sessionName = getSessionName(client);
				String sessionKey = getSessionKey(domain, sessionName);
				client.handle = sessionKey;
				client.sessionName = sessionName;
				client.serverDomain = domain;
				sessionMap.put(sessionKey, client);
				return sessionKey;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public boolean closeSession(String handle) {
		try {
			Optional.ofNullable(sessionMap.remove(handle)).ifPresent(elem -> disconnectSession(elem));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public void closeAllSession() {
		sessionMap.values().forEach(client -> disconnectSession(client));
		sessionMap.clear();
	}
	
	@Override
	public void joined(Runnable unregister) {
		// TODO Auto-generated method stub
		this.unregister = unregister;
	}

	@Override
	public void disjoined() {
		// TODO Auto-generated method stub
		disconnectServer();
	}

}
