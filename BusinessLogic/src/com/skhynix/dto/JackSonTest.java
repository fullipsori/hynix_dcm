package com.skhynix.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JackSonTest {

	@JsonProperty("ID") 
	public String id;
	public String key;
	
	@JsonIgnore
	public String value;

	@JsonProperty("nested_class")
	public NestedJaskSon nested = new NestedJaskSon();

	public class NestedJaskSon {
		
		public String nested_id = "nested_id";
		public String nested_value = "nested_value";

	}

//	public static void main(String[] args) {
//		
//		ObjectMapper mapper = new ObjectMapper()
//				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		JackSonTest testJson = new JackSonTest();
//		testJson.id = "iiiii";
//		testJson.key = "keyyyy";
//		testJson.value = "vvvvvvv";
//		try {
//			String jsonString = mapper.writeValueAsString(testJson);
//			System.out.println("result:" + jsonString);
//			
//			JackSonTest jackson = (JackSonTest) mapper.readValue(jsonString, JackSonTest.class);
//			System.out.println("value:" + jackson.value);
//			
//		} catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
}
