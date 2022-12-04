package com.skhynix.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.skhynix.base.BaseManager;
import com.skhynix.common.StringUtil;
import com.skhynix.extern.Joinable;
import com.skhynix.extern.Pair;

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
	
	public String getMetaData(String joinType, String table, String keyname, long key) {
		
		return (String)resourceManager.retrieve(joinType, table, new Pair<String, Long>(keyname, key));
	}
	
	public boolean putMetaData(String joinType, String table, String keyname,long key, String colname, String colvalue) {
		List<Pair<String,? extends Object>> columns = new ArrayList<>();
		Pair<String, String>  column = new Pair<>(colname, colvalue);
		columns.add(column);
		return resourceManager.create(joinType, table, new Pair<String, Long>(keyname, key), columns);
	}
}
