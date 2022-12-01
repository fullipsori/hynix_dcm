package com.skhynix.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import com.skhynix.decl.BusinessBehavior;
import com.skhynix.manager.MetaDataManager;

/**
 * Proxy 에서 콜하는 Logic Class 이다.
 * @author fullipsori
 *
 */
public class BusinessLogic implements BusinessBehavior {

	private static final BusinessLogic instance = new BusinessLogic();
	private volatile Optional<BusinessBehavior> businessBehavior = Optional.ofNullable(null);
	private final MetaDataManager metaDataManager = MetaDataManager.getInstance();

	public BusinessLogic() { }
	
	public static BusinessLogic getInstance() {
		return instance;
	}

	public void setBusinessBehavior(BusinessBehavior businessBehavior) {
		this.businessBehavior = Optional.ofNullable(businessBehavior);
	}

	@Override
	public String doBusiness(String metaData, String data) throws Exception {
		String converted = metaDataManager.getMetaInfo(metaData);
		try {
			if(businessBehavior.isPresent()) {
				return businessBehavior.get().doBusiness(converted, data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
}
