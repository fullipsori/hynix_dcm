package com.skhynix.neesp.util;

public class SORequestCommand {
	
	public SORequestCommand() {
		
	}
	
	public String make(String command, String parameter, String parameter2, String parameter3, String parameter4) {
		StringBuffer sb = new StringBuffer(20);
		sb.append("{");
		sb.append(String.format("\"command\":\"%s\",", command));
		sb.append(String.format("\"parameter\":\"%s\",",parameter));
		sb.append(String.format("\"parameter2\":\"%s\",",parameter2));
		sb.append(String.format("\"parameter3\":\"%s\",",parameter3));
		sb.append(String.format("\"parameter4\":\"%s\"", parameter4));
		sb.append("}");
		return sb.toString();
	}

}
