package com.skhynix.logic;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skhynix.extern.BusinessBehavior;
import com.skhynix.extern.BusinessSupplier;
import com.skhynix.extern.DynaLoadable;
import com.skhynix.extern.Pair;
import com.skhynix.extern.WaferData;

/**
 * Reloadable Business Logic 으로서 이해를 돕기 위해 만든것이다. 
 * @author fullipsori
 *
 */
public class BusinessLogic implements BusinessBehavior, DynaLoadable {

	@Override
	public String doBusiness(String eventType, String message, Map<String,String> handles, BusinessSupplier supplier, Consumer<Object> resultConsumer) throws Exception {
		/* message 는 json 데이터로 받는것으로 가정한다. 이에 따라서 첫번째로 자신의 WaferData 클래스로 변현환다. */
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		WaferData waferData = mapper.readValue(message, WaferData.class);
		String processResult;
		
		/* waferData 의 메타키 기반으로 AS 로 부터 데이터를 가져온다.  테이블정보는 임시로 my_grid 로 가정하고 key 이름 또한 "key" 로 가정한다. */
		List<Pair<String, String>> params = new ArrayList<>();
		params.add(Pair.of("key", String.valueOf(waferData.metadataKey)));
		String metaValue = (supplier != null || BusinessSupplier.class.isInstance(supplier))?  
				(String)supplier.requestMeta("resource,as", "my_table", params) : "";
		
		/* 값이 없는 경우에는 초기값으로 AS 의 row 를 업데이트 한다. */
		if(metaValue == null || metaValue.isEmpty()) {
			params.clear();
			params.add(Pair.of("key", String.valueOf(waferData.metadataKey)));
			params.add(Pair.of("value", "initial from dynamic business logic"));
			supplier.storeMeta("resource,as", "my_table", params);
			processResult = "initial from dynamic";
		} else {
			/* 값이 있는 경우에는 값을 변경하고 해당 row 값을 업데이트 한다. */ 
			metaValue = String.format("sensor Value:" + Instant.now().toString());
			processResult = String.format("%s: processed", metaValue);
			waferData.setProcessResult(processResult);
			params.clear();
			params.add(Pair.of("key", String.valueOf(waferData.metadataKey)));
			params.add(Pair.of("value", metaValue));
			supplier.storeMeta("resource,as", "my_table", params);
		}

		/* 최종 산출물이 나왔을때 OwnerModule 에서 무언가를 실행주고 싶을때 다음을 실행하는데, 현재는 console에 결과 json string을 출력만 하고 있다. */
		if(resultConsumer != null) {
			resultConsumer.accept(mapper.writeValueAsString(waferData));
		}
		
		/* 마지막으로 결과물을 message router 를 이용하여 보내고 싶을때 아래의 함수를 수행한다.
		 * 만약 타입에 따라서 ems/kafka/ftl 등의 타입이 틀려질때는 아래의 stream 에서 filter 를 적용하여 보내면 된다. 
		 * 이부분은 message handle 을 주고 받는 방안에 대한 협의가 필요하다.
		 *  */

		String[] handleArray = handles.entrySet().stream().map(entry -> entry.getValue()).toArray(String[]::new);
		System.out.println("calling sendAndReceive");
		String jsonRes = supplier.sendAndReceive(handleArray[0], "test.reply", null, message);
//		supplier.sendSyncMessage(handleArray, message);
		
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
