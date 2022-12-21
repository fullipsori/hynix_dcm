package com.skhynix.extern;

import java.util.List;
import java.util.Map;

/**
 * Repository(AS,DB 등) 로 구현하는 클래스들은 하기 함수들을 구현하고 ResourceManager 에 join 하게 되면,
 * ResourceManger 에 의해 관리되고 필요한 경우 호출이 됩니다. 
 * 현재는 AS 가 구현되어 있습니다.
 * @author fullipsori
 *
 */
public interface Resourceable {
	public boolean delete(String handle, String table, Map<String,String> keyValue);
	public boolean create(String handle, String table, Object dtoObject);
	public boolean retrieve(String handle, String table, Map<String,String> keyValue, Object dtoObject);
	public boolean update(String handle, String table, Object dtoObject);
    public <E> List<E> executeSql(String handle, Class<E> clazz, String sqlString);
}
