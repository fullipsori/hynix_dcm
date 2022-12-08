package com.skhynix.model.message;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.skhynix.common.StringUtil;

public class BaseMsgModel {

	public Map<String,String> received = new HashMap<>();
	
	public String toJson() {
		return StringUtil.objectToJson(received);
	}
	
	public String getMessage() {
		return Optional.ofNullable(received.get("message")).orElse("");
	}
	
	/**
	public static String makeJson(String name, Object value) {
		if(String.class.isInstance(value)) 
			return String.format("\"%s\":\"%s\"",  name, value);
		else if(Integer.class.isInstance(value) || Long.class.isInstance(value))
			return String.format("\"%s\":%d", name, value);
		else if(Double.class.isInstance(value) || Float.class.isInstance(value)) 
			return String.format("\"%s\":%f", name, value);
		else if(Byte.class.isInstance(value)) 
			return String.format("\"%s\":%d",name, ((byte)value) & 0xff);
		else return ""; //throw new IllegalArgumentException(value.getClass().getSimpleName());
	}
	**/
}
