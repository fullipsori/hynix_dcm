package com.skhynix.manager;

import java.util.List;
import java.util.Map;

import com.skhynix.base.BaseManager;
import com.skhynix.extern.Joinable;

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
	public void addAction(Object instance) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeAction(String className) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Joinable createMember(String jointype, String serverUrl) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean createMeta(String resourceType, String tableName, Object dtoObject) {
		// TODO Auto-generated method stub
		return resourceManager.create(resourceType, tableName, dtoObject);
	}

	public boolean retrieveMeta(String resourceType, String tableName, Map<String, String> keyValue, Object dtoObject) {
		// TODO Auto-generated method stub
		return resourceManager.retrieve(resourceType, tableName, keyValue, dtoObject);
	}

	public boolean updateMeta(String resourceType, String tableName, Object dtoObject) {
		// TODO Auto-generated method stub
		return resourceManager.update(resourceType, tableName, dtoObject);
	}

	public boolean deleteMeta(String resourceType, String tableName, Map<String, String> keyValue) {
		// TODO Auto-generated method stub
		return resourceManager.delete(resourceType, tableName, keyValue);
	}

	public <E> List<E> executeSql(String resourceType, Class<E> clazz, String sqlString) {
		// TODO Auto-generated method stub
		return resourceManager.executeSql(resourceType, clazz, sqlString);
	}
}
