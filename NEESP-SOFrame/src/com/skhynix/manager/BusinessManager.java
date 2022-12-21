package com.skhynix.manager;

import java.util.function.BiFunction;

import com.skhynix.base.BaseManager;
import com.skhynix.controller.BusinessLogic;
import com.skhynix.extern.BusinessBehavior;
import com.skhynix.extern.Joinable;
import com.skhynix.neesp.log.LogManager;

public class BusinessManager extends BaseManager {
	private static final BusinessManager instance = new BusinessManager();
	private BusinessLogic businessLogic = BusinessLogic.getInstance();
	private LogManager logManager = LogManager.getInstance();
	
	public static BusinessManager getInstance() {
		return instance;
	}
	
	public BusinessManager() { }

	@SuppressWarnings("unchecked")
	@Override
	public void addAction(Object instance) {
		// TODO Auto-generated method stub
		businessLogic.setBusinessBehavior((BiFunction<Object,Object,Object>) instance);
		
	}
	@Override
	public void removeAction(String className) {
		// TODO Auto-generated method stub
		businessLogic.setBusinessBehavior(null);
	}

	@Override
	public String getDomain() {
		// TODO Auto-generated method stub
		return "business";
	}

	@Override
	public Joinable createMember(String jointype, String serverUrl) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
