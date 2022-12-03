package com.skhynix.manager;

import java.util.function.Function;

import com.skhynix.base.BaseManager;
import com.skhynix.common.StringUtil;
import com.skhynix.extern.Joinable;

public class MetaDataManager extends BaseManager {
	private static final MetaDataManager instance = new MetaDataManager();
	private final ResourceManager resourceManager = ResourceManager.getInstance();
	
	public MetaDataManager() {
		// TODO Auto-generated constructor stub
	}
	
	public static MetaDataManager getInstance() {
		return instance;
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
		System.out.println("request:" + request);
		String res = resourceManager.getMetaData("resource:as","my_grid", "key", (Long)request);
		if(StringUtil.isEmpty(res)) {
			resourceManager.putMetaData("resource:as", "my_grid", "key", (Long)request, "value", "stored activespace data");
		}
		res = resourceManager.getMetaData("resource:as","my_grid", "key", (Long)request);
		if(StringUtil.isEmpty(res)) {
			System.out.println("operation failed");
			return "failed:as";
		}
		return res;
	};
}
