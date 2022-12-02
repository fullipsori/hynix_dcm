package com.skhynix.decl;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.hynix.common.StringUtil;

public abstract class BaseManager {
	private final Map<String, Joinable> members = new ConcurrentHashMap<>();
	
	abstract public String getDomain();
	abstract public Joinable createMember(String jointype);

	private boolean isJoinable(String key) {
		return StringUtil.contains(key, getDomain(), ":");
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
	
	public Optional<Object> getMember(String handle) {
		if(!isJoinable(handle)) return Optional.empty();
		return members.entrySet().stream()
				.filter(entry -> handle.startsWith(entry.getKey()))
				.map(entry -> (Object)entry.getValue()).findFirst();
	}
}
