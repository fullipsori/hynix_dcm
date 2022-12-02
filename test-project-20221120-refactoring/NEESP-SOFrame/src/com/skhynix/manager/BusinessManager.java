package com.skhynix.manager;

import java.util.HashMap;
import java.util.Map;

import com.skhynix.controller.BusinessLogic;
import com.skhynix.decl.Joinable;
import com.skhynix.decl.BaseManager;
import com.skhynix.decl.BusinessBehavior;
import com.skhynix.neesp.log.LogManager;

public class BusinessManager extends BaseManager {
	private static final BusinessManager instance = new BusinessManager();
	private BusinessLogic businessLogic = BusinessLogic.getInstance();
	private DynaClassManager dynaClassManager = DynaClassManager.getInstance();
	private LogManager logManager = LogManager.getInstance();
	
	public static BusinessManager getInstance() {
		return instance;
	}
	
	public BusinessManager() {
		dynaClassManager.loadJarSubject
			.filter(jarInfo -> jarInfo.getFirst().startsWith(getDomain()))
			.subscribe(jarInfo -> {
				dynaClassManager.getClassInstance(jarInfo.getSecond()).ifPresent(clazz -> {
					if(clazz instanceof BusinessBehavior) {
						businessLogic.setBusinessBehavior((BusinessBehavior)clazz);
						System.out.println("load jar:" + jarInfo.getSecond()); 
					}
				});
			});

		dynaClassManager.unloadJarSubject
			.filter(jarInfo -> jarInfo.getFirst().startsWith(getDomain()))
			.subscribe(jarInfo -> { 
				businessLogic.setBusinessBehavior(null);
				System.out.println("unload jar:" + jarInfo.getSecond()); 
			});
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
