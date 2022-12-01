package com.skhynix.decl;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.hynix.common.StringUtil;

public abstract class BaseManager {
	private final Map<String, Attendable> attendees = new ConcurrentHashMap<>();
	
	abstract public String getDomain();
	abstract public Attendable createAttendee(String attendType);

	private boolean isJoinable(String key) {
		return StringUtil.contains(key, getDomain(), ":");
	}
	
	/** 모듈이 등록이 않된 경우에만 할당 한다. */
	public void register(String attendType, Object attendee) {
		if(!isJoinable(attendType) || !Attendable.class.isInstance(attendee)) return;
		attendees.putIfAbsent(attendType, (Attendable)attendee);
		((Attendable)attendee).attached(() -> unregister(attendType));
	}
	
	public void unregister(String attendType) {
		if(!isJoinable(attendType)) return;

		Attendable old = (Attendable)attendees.remove(attendType);
		if(old != null) {
			old.detached();
		}
	}
	
	public Optional<Object> getAttendee(String handle) {
		if(!isJoinable(handle)) return Optional.empty();
		return attendees.entrySet().stream()
				.filter(entry -> handle.startsWith(entry.getKey()))
				.map(entry -> (Object)entry.getValue()).findFirst();
	}
}
