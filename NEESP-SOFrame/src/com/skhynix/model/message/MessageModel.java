package com.skhynix.model.message;

import java.io.Serializable;

import com.skhynix.common.StringUtil;

public class MessageModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String jsonProperties = "";
	public String message = null;
	
	public MessageModel applyProperty(String name, Object value) {
		String comma = "";
		if(StringUtil.isNotEmpty(this.jsonProperties)) {
			comma = ",";
		}

		if(String.class.isInstance(value)) 
			this.jsonProperties = String.format("%s%s\"%s\":\"%s\"", this.jsonProperties, comma, name, value);
		else if(Integer.class.isInstance(value) || Long.class.isInstance(value))
			this.jsonProperties = String.format("%s%s\"%s\":%d", this.jsonProperties, comma, name, value);
		else if(Double.class.isInstance(value) || Float.class.isInstance(value)) 
			this.jsonProperties = String.format("%%s\"%s\":%f",this.jsonProperties, comma, name, value);
		else if(Byte.class.isInstance(value)) 
			this.jsonProperties = String.format("%s%s\"%s\":%d",this.jsonProperties, comma, name, ((byte)value) & 0xff);
		else throw new IllegalArgumentException(value.getClass().getSimpleName());
		
		return this;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getProperties() {
		return jsonProperties;
	}
	
	public String toJson() {
		if(StringUtil.isNotEmpty(this.jsonProperties)) {
			System.out.println("toJson:" + this.jsonProperties);
			return String.format("{%s,\"message\":\"%s\"}", this.jsonProperties, (this.message == null)? "" : this.message);
		}else {
			return String.format("{\"message\":\"%s\"}", (this.message == null)? "" : this.message);
		}
	}
	
}
