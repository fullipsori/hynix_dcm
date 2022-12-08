package com.skhynix.logic;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skhynix.dto.TestDTO;
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

	public static final String asResourceType = "resource-as";

	@Override
	public String doBusiness(String eventType, String message, Map<String,String> handles, BusinessSupplier supplier, Consumer<Object> resultConsumer) throws Exception {
		/* message 는 json 데이터로 받는것으로 가정한다. 이에 따라서 첫번째로 자신의 WaferData 클래스로 변현환다. */

		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		WaferData waferData = mapper.readValue(message, WaferData.class);
		String processResult;
		
		/* waferData 의 메타키 기반으로 AS 로 부터 데이터를 가져온다.  테이블정보는 임시로 my_grid 로 가정하고 key 이름 또한 "key" 로 가정한다. */
		TestDTO testDTO = new TestDTO();
		boolean res = supplier.retrieveMeta(asResourceType, "hynix_table", Pair.of("key", "111"), testDTO);
		
		if(!res) {
			testDTO.key = "111111";
			testDTO.value1 = "initialized data";
			testDTO.value2 = "wafer sensor data 2";
			supplier.createMeta(asResourceType, "hynix_table", testDTO);
			processResult = "initial from dynamic";
		}else {
			System.out.println("as result:" + testDTO.value1 + " value2:" + testDTO.value2);
			processResult = String.format("%s processed", testDTO.value1);
		}

		waferData.setProcessResult(processResult);
		
		/* 최종 산출물이 나왔을때 OwnerModule 에서 무언가를 실행주고 싶을때 다음을 실행하는데, 현재는 console에 결과 json string을 출력만 하고 있다. */
		if(resultConsumer != null) {
			resultConsumer.accept(mapper.writeValueAsString(waferData));
		}
		
		/* 마지막으로 결과물을 message router 를 이용하여 보내고 싶을때 아래의 함수를 수행한다.
		 * 만약 타입에 따라서 ems/kafka/ftl 등의 타입이 틀려질때는 아래의 stream 에서 filter 를 적용하여 보내면 된다. 
		 * 이부분은 message handle 을 주고 받는 방안에 대한 협의가 필요하다.
		 *  */

		String[] handleArray = handles.entrySet().stream().map(entry -> entry.getValue()).toArray(String[]::new);
		Map<String,String> properties = new HashMap<>();
		properties.put("testProperty", "abcdef");

		if(handleArray[0].startsWith("message-ems")) {
			String jsonRes = supplier.sendAndReceive(handleArray[0], message, properties,  "test.reply", null, 60000);
		}
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
