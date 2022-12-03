package com.skhynix.logic;

import java.util.function.Consumer;
import java.util.function.Function;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skhynix.extern.BusinessBehavior;
import com.skhynix.extern.DynaLoadable;
import com.skhynix.extern.WaferData;

public class BusinessLogic implements BusinessBehavior, DynaLoadable {

	@Override
	public String doBusiness(String eventType, String message, Function<Object, Object> metaSource, Consumer<Object> resultConsumer) throws Exception {
		// TODO Auto-generated method stub
		/** business Logic **/

		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		WaferData waferData = mapper.readValue(message, WaferData.class);
		
		String metaValue = (metaSource != null)?  (String)metaSource.apply(waferData.metadataKey) : "";
		
		if(metaValue == null || metaValue.isEmpty()) {
			
		}

		String procResult = String.format("processed - %s:%s", metaValue, "ok");
		waferData.setProcessResult(procResult);

		if(resultConsumer != null) {
			resultConsumer.accept(mapper.writeValueAsString(waferData));
		}
		return procResult;
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
