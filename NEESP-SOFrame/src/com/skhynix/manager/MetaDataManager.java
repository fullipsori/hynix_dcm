package com.skhynix.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.skhynix.base.BaseManager;
import com.skhynix.common.StringUtil;
import com.skhynix.extern.Joinable;
import com.skhynix.extern.Pair;

/**
 * 메타데이터 매니저는 데이터를 로컬로 관리하는 역할도 진행한다.  즉 데이터가 hit 되는 경우에는 Map에서 아닌경우에만 AS 를 접속한다.
 * 이후 구현이 필요한 항목이다.
 * @author fullipsori
 *
 */
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
	
	public boolean createMetaData(String sourcetype, String table, Object dtoObject) {
		// TODO Auto-generated method stub
		return resourceManager.create(sourcetype, table, dtoObject);
	}

	public boolean retrieveMeta(String sourcetype, String table, Pair<String, String> key, Object dtoObject) {
		// TODO Auto-generated method stub
		return resourceManager.retrieve(sourcetype, table, key, dtoObject);
	}

	public boolean updateMeta(String sourcetype, String table, Object dtoObject) {
		// TODO Auto-generated method stub
		return resourceManager.update(sourcetype, table, dtoObject);
	}

	public boolean deleteMeta(String sourcetype, String table, Pair<String, ? extends Object> key) {
		// TODO Auto-generated method stub
		return resourceManager.delete(sourcetype, table, key);
	}

	public <E> List<E> executeSql(String sourcetype, Class<E> clazz, String sqlString) {
		// TODO Auto-generated method stub
		return resourceManager.executeSql(sourcetype, clazz, sqlString);
	}
}
