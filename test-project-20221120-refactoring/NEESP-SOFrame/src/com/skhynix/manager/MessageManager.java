package com.skhynix.manager;

import java.util.Optional;

import com.skhynix.decl.Joinable;
import com.skhynix.decl.BaseManager;
import com.skhynix.decl.Messageable;
import com.skhynix.decl.Sessionable;
import com.skhynix.messaging.EmsMessage;
import com.skhynix.messaging.FtlMessage;
import com.skhynix.messaging.KafkaMessage;
import com.skhynix.model.BaseSessModel;
import com.skhynix.neesp.log.LogManager;

public class MessageManager extends BaseManager implements Messageable, Sessionable {
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
	

	@Override
	public String getDomain() {
		return "message";
	}

	@Override
	public Joinable createMember(String jointype, String serverUrl) {
		String connectionInfo = String.format("%s:%s", jointype, serverUrl);
		switch(jointype) {
			case "message:ems" : return new EmsMessage(connectionInfo); 
			case "message:ftl" : return new FtlMessage(connectionInfo); 
			case "message:kafka" : return new KafkaMessage(connectionInfo); 
		}
		return null;
	}

	@Override
	public boolean sendMessage(String handle, String message) {
		return getMember(handle).map(client -> {
			if(client != null && Messageable.class.isInstance(client))
				return ((Messageable)client).sendMessage(handle, message);
			else return false;
		}).get();
	}
	
	@Override
	public String receiveMessage(String sessionKey) {
		return getMember(sessionKey).map(client -> {
			if(client != null && Messageable.class.isInstance(client))
				return ((Messageable)client).receiveMessage(sessionKey);
			else return "error";
		}).get();
	}
	
	@Override
	public String openSession(String jointype, String serverUrl, String jsonParams) {
		String handle = String.format("%s:%s", jointype, serverUrl);
		
		return getMember(handle).or(() -> {
			Optional<Joinable> client = Optional.ofNullable(createMember(jointype, serverUrl));
			client.ifPresent(c -> register(jointype, c));
			return client;
		}).filter(Sessionable.class::isInstance).map(client -> ((Sessionable)client).openSession(jointype, serverUrl, jsonParams)).get();
	}

	@Override
	public boolean closeSession(String handle) {
		return getMember(handle).map(client -> {
			if(client != null && Sessionable.class.isInstance(client))
				return ((Sessionable)client).closeSession(handle);
			else return false;
		}).get();
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
}
