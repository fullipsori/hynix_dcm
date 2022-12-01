package com.test;

import java.util.function.Function;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WaperData implements Function<Object,Object> {

	public static TestJson testJson = null;

    private static final WaperData instance = new WaperData();
    public static WaperData getInstance() {
        return instance;
    }

    public static boolean initialize(String[] args) {
		String jsonStr = "{\"id\" : 1, \"name\" : \"Sanghoon\"}";
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			testJson = objectMapper.readValue(jsonStr, TestJson.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
    }
    
    public static boolean deInitialize() {
    	testJson = null;
    	return true;
    }

	@Override
	public Object apply(Object t) {
		// TODO Auto-generated method stub
		String param = (String) t;
		if(param.equals("id")) return testJson.getId();
		else if(param.equals("name")) return testJson.getName();
		else return testJson.toString();
	}

}
