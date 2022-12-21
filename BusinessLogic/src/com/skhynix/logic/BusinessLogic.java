package com.skhynix.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import com.skhynix.dto.JackSonTest;
import com.skhynix.dto.TestDTO;

public class BusinessLogic implements BiFunction<Map<String,Object>, BiFunction<String, Object, Object>, String> {

	private static final BusinessAdapter businessAdapter = BusinessAdapter.getInstance();

	@Override
	public String apply(Map<String, Object> received, BiFunction<String, Object, Object> functionSupplier) {
		// TODO Auto-generated method stub
		return doBusiness(received, functionSupplier);
	}
	
	@SuppressWarnings("unchecked")
	public String doBusiness(Map<String, Object> received, BiFunction<String, Object, Object> functionSupplier) {
		String processResult;

		String eventType = (String)received.get("eventType");
		String message = (String)received.get("message");
		System.out.println("processing: " + eventType + " message:" + message);

		TestDTO testDTO = new TestDTO();
		Map<String, String> keyValue = Map.of("key", "111");
		boolean res = businessAdapter.retrieveMeta(functionSupplier, BusinessAdapter.asResourceType, "hynix_table", keyValue, testDTO);
		
		if(!res) {
			testDTO.key = "111111";
			testDTO.value1 = "initialized data";
			testDTO.value2 = "wafer sensor data 2";

			businessAdapter.createMeta(functionSupplier, BusinessAdapter.asResourceType, "hynix_table", testDTO);
			processResult = "initial from dynamic";
		}else {
			System.out.println("as result:" + testDTO.value1 + " value2:" + testDTO.value2);
			processResult = String.format("%s processed", testDTO.value1);
		}

		/* 마지막으로 결과물을 message router 를 이용하여 보내고 싶을때 아래의 함수를 수행한다.
		 * 만약 타입에 따라서 ems/kafka/ftl 등의 타입이 틀려질때는 아래의 stream 에서 filter 를 적용하여 보내면 된다. 
		 * 이부분은 message handle 을 주고 받는 방안에 대한 협의가 필요하다.
		 *  */
		
		Map<String,String> handles = (Map<String,String>)received.get("handles");

		Optional.ofNullable(handles.get("ems")).ifPresent(handle -> {
			Map<String,String> properties = new HashMap<>();
			properties.put("testProperty", "abcdef");
			String jsonRes = businessAdapter.sendAndReceive(functionSupplier, handle, message, properties, "test.reply", null, 60000);
			System.out.println("sendAndReceive:" + jsonRes);
			Map<String, String> testJson = Map.ofEntries(Map.entry("test1", "test11111"), Map.entry("test2", "test2"));
			
			String jsonString = businessAdapter.mapToJson(functionSupplier, testJson);
			System.out.println("test mapToJson:" + jsonString);
			
			Map<String, String> testMap = businessAdapter.jsonToMap(functionSupplier, jsonString);
			System.out.println("test jsonToMap:" + testMap.get("test1") + " test2:" + testMap.get("test2"));

			JackSonTest jackSon = new JackSonTest();
			jackSon.id = "sanghoon";
			jackSon.key = "key is";
			jackSon.value = "value is";
			
			String jsonTestString = (String) businessAdapter.objToJson(functionSupplier, jackSon);
			System.out.println("result:" + jsonTestString);
			
			JackSonTest jackSonTest2 = (JackSonTest) businessAdapter.jsonToObj(functionSupplier, jsonTestString, JackSonTest.class);
			System.out.println("ttttt:" + jackSonTest2.nested.nested_id);
		});
		

		return processResult;
	}
	
}
