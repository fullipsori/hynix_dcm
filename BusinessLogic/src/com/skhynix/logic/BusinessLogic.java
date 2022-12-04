package com.skhynix.logic;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skhynix.extern.BusinessBehavior;
import com.skhynix.extern.DynaLoadable;
import com.skhynix.extern.MetaFunction;
import com.skhynix.extern.Pair;
import com.skhynix.extern.WaferData;

public class BusinessLogic implements BusinessBehavior, DynaLoadable {

	@Override
	public String doBusiness(String eventType, String message, MetaFunction function, Consumer<Object> resultConsumer) throws Exception {
		// TODO Auto-generated method stub
		/** business Logic **/
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		WaferData waferData = mapper.readValue(message, WaferData.class);
		String processResult;
		
		List<Pair<String, String>> params = new ArrayList<>();
		params.add(new Pair<String, String>("key", String.valueOf(waferData.metadataKey)));
		String metaValue = (function != null || MetaFunction.class.isInstance(function))?  
				(String)function.requestMeta("resource:as", "my_grid", params) : "";
		
		if(metaValue == null || metaValue.isEmpty()) {
			params.clear();
			params.add(new Pair<String, String>("key", String.valueOf(waferData.metadataKey)));
			params.add(new Pair<String, String>("value", "initial from dynamic business logic"));
			function.storeMeta("resource:as", "my_grid", params);
			processResult = "initial from dynamic";
		} else {
			metaValue = String.format("sensor Value:" + Instant.now().toString());
			processResult = String.format("%s: processed", metaValue);
			waferData.setProcessResult(processResult);
			params.clear();
			params.add(new Pair<String, String>("key", String.valueOf(waferData.metadataKey)));
			params.add(new Pair<String, String>("value", metaValue));
			function.storeMeta("resource:as", "my_grid", params);
		}

		if(resultConsumer != null) {
			resultConsumer.accept(mapper.writeValueAsString(waferData));
		}
		return processResult;
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
