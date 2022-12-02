package com.hynix.logic;

import java.util.function.Consumer;
import java.util.function.Function;

import com.skhynix.extern.BusinessBehavior;
import com.skhynix.extern.DynaLoadable;

public class BusinessLogic implements BusinessBehavior, DynaLoadable {

	@Override
	public String doBusiness(String eventType, String message, Function<Object, Object> metaSource,
			Consumer<Object> resultConsumer) throws Exception {
		// TODO Auto-generated method stub
		String metaData = (metaSource != null)?  (String)metaSource.apply(eventType) : "";
		String result = String.format("%s - %s", metaData, message);
		if(resultConsumer != null) {
			resultConsumer.accept(result);
		}
		return result;
	}

	@Override
	public void loadClass() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unloadClass() {
		// TODO Auto-generated method stub
		
	}

}
