package com.skhynix.decl;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.skhynix.model.BaseSessModel;

abstract public class BaseConnection implements Sessionable, Joinable {

	protected Runnable unregister = null;

	protected BaseSessModel serverModel = null;
	protected Map<String, BaseSessModel> clientMap = new ConcurrentHashMap<>();
	protected String connectionInfo;
	
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
	
	private String getSessionKey(String domain, String sessionName) {
		return String.format("%s:%s:%s", domain, sessionName, getRandomKey());
	}
	
	@Override
	public String openSession(String domain, String serverUrl, String jsonParams) {

		if(serverModel == null) {
			initConnectable(domain, jsonParams);
		}

		BaseSessModel client = makeSessModel(domain, jsonParams);

		try {
			client = connectSession(client);
			String sessionKey = getSessionKey(domain, getSessionName(client));
			if(client != null) {
				clientMap.put(sessionKey, client);
			} else {
				return null;
			}
			return sessionKey;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public boolean closeSession(String sessionKey) {
		try {
			Optional.ofNullable(clientMap.remove(sessionKey)).ifPresent(elem -> disconnectSession(elem));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public void closeAllSession() {
		clientMap.values().forEach(client -> disconnectSession(client));
		clientMap.clear();
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
