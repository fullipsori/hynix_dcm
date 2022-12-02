package com.skhynix.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.skhynix.base.BaseManager;
import com.skhynix.extern.Joinable;

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

	public String getServerInfo(String serverKey) {
		return serverInfo.getOrDefault(serverKey, "");
	}
	
	public String getMetaInfo(String resourceKey) {
		return resourceInfo.computeIfAbsent(resourceKey, resource -> "initial");
	}

	@Override
	public String getDomain() {
		// TODO Auto-generated method stub
		return "meta";
	}

	@Override
	public Joinable createMember(String jointype, String serverUrl) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Function<Object,Object> defaultDataSourcer = request -> {
		switch((String)request) {
			case "1" : return "supply 1";
			case "2" : return "supply_2;";
			default : return "supply_unknown";
		}
	};
}
