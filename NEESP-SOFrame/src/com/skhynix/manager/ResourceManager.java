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
	public Joinable createMember(String jointype, String serverUrl) {
		// TODO Auto-generated method stub
		switch(jointype) {
			case "resource,as" : return ASRepository.getInstance(); 
		}
		return null;
	}

	@Override
	public String openSession(String jointype, String serverUrl, String jsonParams) {
		String domain = String.format("%s%s%s", jointype, defaultDelimiter, serverUrl);
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
	@Override
	public boolean create(String joinType, String table, Pair<String, ? extends Object> key,
			List<Pair<String, ? extends Object>> params) {
		// TODO Auto-generated method stub
		String handle = resourceMap.get(joinType);
		if(StringUtil.isEmpty(handle)) return false;
		Object client = getMember(handle);
		if(Resourceable.class.isInstance(client)) {
			return ((Resourceable)client).create(handle, table, key,  params);
		}
		return false;
	}
	@Override
	public Object retrieve(String joinType, String table, Pair<String, ? extends Object> key) {
		// TODO Auto-generated method stub
		String handle = resourceMap.get(joinType);
		if(StringUtil.isEmpty(handle)) return "unknown joinType :" + joinType;
		Object client = getMember(handle);
		if(Resourceable.class.isInstance(client)) {
			return (String)((Resourceable)client).retrieve(handle, table, key);
		}
		return "";
	}
	@Override
	public boolean update(String handle, String table, Pair<String, ? extends Object> key,
			List<Pair<String, ? extends Object>> params) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean delete(String handle, String table, Pair<String, ? extends Object> key) {
		// TODO Auto-generated method stub
		return false;
	}
}
