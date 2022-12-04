package com.skhynix.manager;

import java.util.Optional;

import com.skhynix.base.BaseManager;
import com.skhynix.extern.Joinable;
import com.skhynix.extern.Messageable;
import com.skhynix.extern.SessionBehavior;
import com.skhynix.messaging.EmsMessage;
import com.skhynix.messaging.FtlMessage;
import com.skhynix.messaging.KafkaMessage;
import com.skhynix.model.session.BaseSessModel;
import com.skhynix.neesp.log.LogManager;

public class MessageManager extends BaseManager implements Messageable, SessionBehavior {
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
				Optional.ofNullable(dynaClassManager.getClassInstance(jarInfo.getSecond())).ifPresent(clazz -> {
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
		switch(jointype) {
			case "message:ems" : return new EmsMessage(jointype, serverUrl); 
			case "message:ftl" : return new FtlMessage(jointype, serverUrl); 
			case "message:kafka" : return new KafkaMessage(jointype, serverUrl); 
		}
		return null;
	}

	@Override
	public boolean sendMessage(String handle, String message) {
		Object client = getMember(handle);
		if(client != null && Messageable.class.isInstance(client))
			return ((Messageable)client).sendMessage(handle, message);
		else return false;
	}
	
	@Override
	public String receiveMessage(String handle) {
		Object client = getMember(handle);
		if(client != null && Messageable.class.isInstance(client))
			return ((Messageable)client).receiveMessage(handle);
		else return "";
	}
	
	@Override
	public void confirmMessage(String handle) {
		// TODO Auto-generated method stub
		Object client = getMember(handle);
		if(client != null && Messageable.class.isInstance(client))
			((Messageable)client).confirmMessage(handle);
	}

	@Override
	public String openSession(String jointype, String serverUrl, String jsonParams) {
		String domain = String.format("%s:%s", jointype, serverUrl);
		Object client = getMember(domain);
		if(client == null) {
			client = Optional.ofNullable(createMember(jointype, serverUrl)).map(c -> {
				register(domain, c);
				return c;
			}).orElse(null);
		}
		return (client != null && SessionBehavior.class.isInstance(client)) ? 
				((SessionBehavior)client).openSession(domain, serverUrl, jsonParams) : "";
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
}
