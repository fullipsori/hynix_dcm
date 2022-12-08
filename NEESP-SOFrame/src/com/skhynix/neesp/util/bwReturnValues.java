package com.skhynix.neesp.util;

public class bwReturnValues {
	
//	private static bwReturnValues instance = new bwReturnValues();
//	public static bwReturnValues getInstance() {return instance; }
//	private bwReturnValues() {};
	
	public bwReturnValues () {
		
	}
	
	public String[] retVal1(String code, String value) {
		String[] retVals = {"",""};
		retVals[0] = code;
		retVals[1] = value;
		return retVals;
	}
	
	public String[] retVal2(String code, String value1, String value2) {
		System.err.printf("/// [%s][%s][%s] 값을 설정합니다.\n", code, value1, value2);
		String[] retVals = {"","",""};
		retVals[0] = code;
		retVals[1] = value1;
		retVals[2] = value2;		
		return retVals;
	}
	
	public String[] retVal3(String code, String value1, String value2, String value3) {
		String[] retVals = {"","","",""};
		retVals[0] = code;
		retVals[1] = value1;
		retVals[2] = value2;
		retVals[3] = value3;
		return retVals;
	}
	
	public String[] retVal4(String code, String value1, String value2, String value3, String values4) {
		String[] retVals = {"","","","",""};
		retVals[0] = code;
		retVals[1] = value1;
		retVals[2] = value2;
		retVals[4] = value3;
		return retVals;
	}
}
