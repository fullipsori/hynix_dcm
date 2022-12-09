package com.skhynix.manager;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.skhynix.base.BaseManager;
import com.skhynix.common.StringUtil;
import com.skhynix.extern.Joinable;
import com.skhynix.extern.Pair;
import com.skhynix.extern.Resourceable;
import com.skhynix.extern.SessionBehavior;
import com.skhynix.model.session.BaseSessModel;
import com.skhynix.repository.ASRepository;

public class ResourceManager extends BaseManager implements SessionBehavior, Resourceable {
	private static final ResourceManager instance = new ResourceManager();
//	private final ASRepository asRepository = ASRepository.getInstance();
//	private final DBRepository dbRepository = DBRepository.getInstance();
//	private final FileRepository fileRepository = FileRepository.getInstance();
//	private final HttpRepository httpRepository = HttpRepository.getInstance();
//	private final MetaDataManager metaDataManager = MetaDataManager.getInstance();

	private Map<String, String> resourceMap = new ConcurrentHashMap<>();

	public ResourceManager() {
		// TODO Auto-generated constructor stub
	}
	public static ResourceManager getInstance() {
		return instance;
	}
	
	public void initRepos() {
	}
	
	@Override
	public String getDomain() {
		// TODO Auto-generated method stub
		return "resource";
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
		String resourcePrefix = "resource" + BaseSessModel.defaultDelimiter;
		String messageType = jointype.substring(resourcePrefix.length());
		switch(messageType) {
			case "as" : return ASRepository.getInstance(); 
		}
		return null;
	}

	@Override
	public String openSession(String jointype, String serverUrl, String jsonParams) {
		String domain = String.format("%s%s%s", jointype, BaseSessModel.defaultDelimiter, serverUrl);
		Object client = getMember(domain);
		if(client == null) {
			client = Optional.ofNullable(createMember(jointype, serverUrl)).map(c -> {
				register(domain, c);
				return c;
			}).orElse(null);
		}
		String handle = (client != null && SessionBehavior.class.isInstance(client)) ? 
				((SessionBehavior)client).openSession(domain, serverUrl, jsonParams) : "";

		if(!StringUtil.isEmpty(handle)) resourceMap.put(jointype, handle);
		return handle;
	}

	@Override
	public boolean closeSession(String handle) {
		Object client = getMember(handle);
		if(client != null && SessionBehavior.class.isInstance(client))
			return ((SessionBehavior)client).closeSession(handle);
		else return false;
	}
	
	@Override
	public BaseSessModel makeSessModel(String domain, String jsonParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean connectServer(BaseSessModel client) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void disconnectServer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSessionName(BaseSessModel client) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseSessModel connectSession(BaseSessModel client) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disconnectSession(BaseSessModel client) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeAllSession() {
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings("unused")
	private Optional<Pair<Resourceable,String>> getClient(String joinType) {
		String handle = resourceMap.get(joinType);
		if(StringUtil.isEmpty(handle)) return null;
		Object client = getMember(handle);
		if(Resourceable.class.isInstance(client)) {
			return Optional.of(Pair.of((Resourceable)client, handle));
		}
		return Optional.empty();
	}
	
	@Override
	public boolean create(String joinType, String table, Pair<String, ? extends Object> key,
			List<Pair<String, ? extends Object>> params) {
		return getClient(joinType).map(pair -> pair.getFirst().create(pair.getSecond(), table, key, params)).orElse(false);
	}
	@Override
	public Object retrieve(String joinType, String table, Pair<String, ? extends Object> key) {
		// TODO Auto-generated method stub
		return getClient(joinType).map(pair -> pair.getFirst().retrieve(pair.getSecond(), table, key)).orElse("");
	}
	@Override
	public boolean update(String jointype, String table, Pair<String, ? extends Object> key,
			List<Pair<String, ? extends Object>> params) {
		return getClient(jointype).map(pair -> pair.getFirst().update(pair.getSecond(), table, key, params)).orElse(false);
	}
	@Override
	public boolean delete(String jointype, String table, Pair<String, ? extends Object> key) {
		// TODO Auto-generated method stub
		return getClient(jointype).map(pair -> pair.getFirst().delete(pair.getSecond(), table, key)).orElse(false);
	}
	@Override
	public boolean create(String jointype, String table, Object dtoObject) {
		// TODO Auto-generated method stub
		return getClient(jointype).map(pair -> pair.getFirst().create(pair.getSecond(), table, dtoObject)).orElse(false);
	}
	@Override
	public boolean retrieve(String jointype, String table, Pair<String, String> key, Object dtoObject) {
		// TODO Auto-generated method stub
		return getClient(jointype).map(pair -> pair.getFirst().retrieve(pair.getSecond(), table, key, dtoObject)).orElse(false);
	}
	@Override
	public boolean update(String jointype, String table, Object dtoObject) {
		// TODO Auto-generated method stub
		return getClient(jointype).map(pair -> pair.getFirst().update(pair.getSecond(), table, dtoObject)).orElse(false);
	}
	@Override
	public <E> List<E> executeSql(String jointype, Class<E> clazz, String sqlString) {
		// TODO Auto-generated method stub
		return getClient(jointype).map(pair -> pair.getFirst().executeSql(pair.getSecond(), clazz, sqlString)).orElse(null);
	}
}
