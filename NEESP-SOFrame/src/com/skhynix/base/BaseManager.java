package com.skhynix.base;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.skhynix.common.StringUtil;
import com.skhynix.extern.DynaLoadable;
import com.skhynix.extern.Joinable;
import com.skhynix.manager.DynaClassManager;
import com.skhynix.model.session.BaseSessModel;

public abstract class BaseManager {
	private final Map<String, Joinable> members = new ConcurrentHashMap<>();
	
	public BaseManager() {
		setupDynaLoading();
	}
	
	public void setupDynaLoading() {
		DynaClassManager.getInstance().loadJarSubject
			.filter(jarInfo -> jarInfo.getFirst().startsWith(getDomain()))
			.subscribe(jarInfo -> {
				Object instance = DynaClassManager.getInstance().getClassInstance(jarInfo.getSecond());
				if(instance != null) {
					if(DynaLoadable.class.isInstance(instance)) ((DynaLoadable)instance).getClass();
					register(jarInfo.getFirst(), instance);
					addAction(instance);
				}
				System.out.println("load jar:" + jarInfo.getSecond()); 
			});

		DynaClassManager.getInstance().unloadJarSubject
			.filter(jarInfo -> jarInfo.getFirst().startsWith(getDomain()))
			.subscribe(jarInfo -> { 
				unregister(jarInfo.getFirst());
				System.out.println("unload jar:" + jarInfo.getSecond()); 
			});
	}
	
	abstract public String getDomain();
	abstract public void addAction(Object instance);
	abstract public void removeAction(String className);
	abstract public Joinable createMember(String jointype, String serverUrl);

	private boolean isJoinable(String key) {
		return StringUtil.contains(key, getDomain(), BaseSessModel.defaultDelimiter);
	}
	
	/** 모듈이 등록이 않된 경우에만 할당 한다. */
	public void register(String jointype, Object member) {
		if(!isJoinable(jointype) || !Joinable.class.isInstance(member)) return;
		members.putIfAbsent(jointype, (Joinable)member);
		((Joinable)member).joined(() -> unregister(jointype));
	}
	
	public void unregister(String jointype) {
		if(!isJoinable(jointype)) return;

		Joinable old = (Joinable)members.remove(jointype);
		if(old != null) {
			old.disjoined();
		}
	}
	
	public Object getMember(String handle) {
		if(!isJoinable(handle)) return null;
		return members.entrySet().stream()
				.filter(entry -> handle.startsWith(entry.getKey()))
				.map(entry -> (Object)entry.getValue()).findFirst().orElse(null);
	}
	
}
