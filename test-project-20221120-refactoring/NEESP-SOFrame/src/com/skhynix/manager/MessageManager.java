package com.skhynix.manager;

import java.util.Optional;

import com.skhynix.decl.Attendable;
import com.skhynix.decl.BaseManager;
import com.skhynix.decl.DynaLoadable;
import com.skhynix.decl.Messageable;
import com.skhynix.decl.Sessionable;
import com.skhynix.messaging.EmsMessage;
import com.skhynix.messaging.FtlMessage;
import com.skhynix.messaging.KafkaMessage;
import com.skhynix.neesp.log.LogManager;

public class MessageManager extends BaseManager {
	private static final MessageManager instance = new MessageManager();
	private final DynaClassManager dynaClassManager = DynaClassManager.getInstance();
	private final LogManager logManager = LogManager.getInstance();
	
	public MessageManager() {
		// TODO Auto-generated constructor stub
		registerObserver();
	}
	
	public void registerObserver() {
		dynaClassManager.loadJarSubject
			.filter(jarInfo -> jarInfo.getFirst().startsWith(getDomain()))
			.subscribe(jarInfo -> {
				dynaClassManager.getClassInstance(jarInfo.getSecond()).ifPresent(clazz -> {
					register(jarInfo.getFirst(), clazz);
					System.out.println("load jar:" + jarInfo.getSecond()); 
				});
			});

		dynaClassManager.unloadJarSubject
			.filter(jarInfo -> jarInfo.getFirst().startsWith(getDomain()))
			.subscribe(jarInfo -> { 
				unregister(jarInfo.getFirst());
				System.out.println("unload jar:" + jarInfo.getSecond()); 
			});
		
	}
	
	public static MessageManager getInstance() {
		return instance;
	}
	
	public String openSession(String attendType, String jsonParams) {
		
		return getAttendee(attendType).or(() -> {
			Optional<Attendable> client = Optional.ofNullable(createAttendee(attendType));
			client.ifPresent(c -> register(attendType, c));
			return client;
		}).filter(Sessionable.class::isInstance).map(client -> ((Sessionable)client).openSession(attendType, jsonParams)).get();
	}

	public boolean closeSession(String handle) {
		return getAttendee(handle).map(client -> {
			if(client != null && Sessionable.class.isInstance(client))
				return ((Sessionable)client).closeSession(handle);
			else return false;
		}).get();
	}
	
	public boolean sendMessage(String handle, String message) {
		return getAttendee(handle).map(client -> {
			if(client != null && Messageable.class.isInstance(client))
				return ((Messageable)client).sendMessage(handle, message);
			else return false;
		}).get();
	}
	
	public String receiveMessage(String handle) {
		return getAttendee(handle).map(client -> {
			if(client != null && Messageable.class.isInstance(client))
				return ((Messageable)client).receivedMessage(handle);
			else return "error";
		}).get();
	}
	

	@Override
	public String getDomain() {
		return "message";
	}

	@Override
	public Attendable createAttendee(String attendType) {
		switch(attendType) {
			case "message:ems" : return new EmsMessage(); 
			case "message:ftl" : return new FtlMessage(); 
			case "message:kafka" : return new KafkaMessage(); 
		}
		return null;
	}
	
}
