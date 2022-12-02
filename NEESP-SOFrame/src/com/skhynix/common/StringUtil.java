package com.skhynix.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StringUtil {
	
	public static <T> String objectToJson(T object) {
		try {
			ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			return mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static <T> T jsonToObject(String jsonString, Class<T> clazzType) {
		try {
			ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			return mapper.readValue(jsonString, clazzType);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean isEmpty(String data) {
		if(data == null || data.isEmpty()) return true;
		return false;
	}
	
	public static boolean contains(String[] dataSet, String data) {
		if(dataSet == null) return false;
		return Arrays.stream(dataSet).anyMatch(data::equals);
	}
	
	public static boolean contains(String dataSet, String data, String delimiter) {
		String[] dataArray = (isEmpty(dataSet))? null : dataSet.split(delimiter);	
		if(dataArray == null) return false;
		return contains(dataArray, data);
	}
	
	public static boolean containsAll(String targetSet, String checkSet, String delimiter) {
		String[] firstArray = (isEmpty(targetSet))? null : targetSet.split(delimiter);
		String[] secondArray = (isEmpty(checkSet))? null : checkSet.split(delimiter);
		
		if(firstArray == null || secondArray == null) return false;

		Set<String> firstS = (firstArray != null)? new HashSet<String>(Arrays.asList(firstArray)) : new HashSet<String>();
		Set<String> secondS = (secondArray != null)? new HashSet<String>(Arrays.asList(secondArray)) : new HashSet<String>();
		return firstS.containsAll(secondS);
	}
}
