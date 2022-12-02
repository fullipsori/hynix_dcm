package com.skhynix.decl;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.hynix.common.StringUtil;
import com.skhynix.model.BaseSessModel;

abstract public class BaseConnection implements Sessionable, Joinable {

	protected Runnable unregister = null;

	protected BaseSessModel serverModel = null;
	protected Map<String, BaseSessModel> clientMap = new ConcurrentHashMap<>();
	protected String connectableType;
	
	abstract protected BaseSessModel initParams(String jsonParams);
	abstract protected boolean connectServer(BaseSessModel client);
	abstract protected void disconnectServer();

	abstract protected String getSessionName(BaseSessModel client);
	abstract protected BaseSessModel connectSession(BaseSessModel client);
	abstract protected void disconnectSession(BaseSessModel client);

	public String getConnectableType() {
		return connectableType;
	}

	protected void initConnectable(String jsonParams) {
		serverModel = initParams(jsonParams);
		connectServer(serverModel);
	}
	
	protected void deInitConnectable() {
		
		disconnectServer();
		serverModel = null;
	}

	private String getRandomKey() {
		return String.valueOf(ThreadLocalRandom.current().nextInt(100000));
	}
	
	private String getSessionKey(String prefixKey, String sessionName) {
		return String.format("%s:%s:%s", prefixKey, sessionName, getRandomKey());
	}
	
	@Override
	public String openSession(String prefixKey, String jsonParams) {

		if(serverModel == null) {
			initConnectable(jsonParams);
		}

		BaseSessModel client = initParams(jsonParams);

		try {
			client = connectSession(client);
			String sessionKey = getSessionKey(prefixKey, getSessionName(client));
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
