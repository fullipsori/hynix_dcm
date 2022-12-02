package com.skhynix.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.skhynix.decl.Joinable;
import com.skhynix.decl.BaseManager;

public class MetaDataManager extends BaseManager {
	private static final MetaDataManager instance = new MetaDataManager();
	private Map<String, String> serverInfo = new HashMap<>();
	private Map<String, String> resourceInfo = new HashMap<>();
	
	public MetaDataManager() {
		// TODO Auto-generated constructor stub
	}
	
	public static MetaDataManager getInstance() {
		return instance;
	}

	public void setServerInfo(String serverKey, String info) {
		serverInfo.put(serverKey, info);
	}

	public Optional<String> getServerInfo(String serverKey) {
		return Optional.ofNullable(serverInfo.get(serverKey));
	}
	
	public String getMetaInfo(String resourceKey) {
		return Optional.ofNullable(resourceInfo.get(resourceKey)).map(data -> {
			if(data == null) {
				resourceInfo.put(resourceKey, "initial");
				return "initial";
			} else {
				return data;
			}
		}).get();
	}

	@Override
	public String getDomain() {
		// TODO Auto-generated method stub
		return "meta";
	}

	@Override
	public Joinable createMember(String jointype) {
		// TODO Auto-generated method stub
		return null;
	}
}
