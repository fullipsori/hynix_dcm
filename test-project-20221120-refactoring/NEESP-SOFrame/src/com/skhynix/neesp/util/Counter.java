package com.skhynix.neesp.util;

import java.util.HashMap;
import java.util.Map;

public class Counter {
	
	private class Count {
		private long countVal=0;
		public Count(long initVal) {
			countVal=initVal;
		}
		public void increase() {countVal++;};
		public void decrease() {countVal--;};
		public void reset() {countVal = 0;}
		public long getCount() { return countVal; }
	}
	
	private Map<String, Count> mapCOUNTER = new HashMap<>();
	private String counterName = "";
	
	public Counter(String counterName) {
		this.counterName = counterName;
	}
	
	public long getCount(String keyVal) {
		Count count = mapCOUNTER.get(keyVal);
		if(count!=null) return count.getCount();
		else {
			return -99999; 
		}
	}
	
	public void resetCountAll() {		
		for(String eqpId : mapCOUNTER.keySet()) {
			mapCOUNTER.get(eqpId).reset();
		}
	}
	
	public void resetCount(String keyVal) {
		Count count = mapCOUNTER.get(keyVal);
		if(count!=null) count.reset(); 
	}
	
	public void increaseCount(String keyVal) {
		Count count = mapCOUNTER.get(keyVal);
		if(count!=null) count.increase();
		else {
			mapCOUNTER.put(keyVal, new Count(1));
		}
	}
	
	public void decreaseCount(String keyVal) {
		Count count = mapCOUNTER.get(keyVal);
		if(count!=null) count.increase();
		else {
			mapCOUNTER.put(keyVal, new Count(-1));
		}
	}
	
	public String printCounter(String keyVal) {
		Count count = mapCOUNTER.get(keyVal);
		if(count!=null)	return String.format("{\"%s\": %d}", keyVal, count.getCount());
		else return String.format("{\"%s\": %d}", keyVal, -99999); // 값이 없는 겨우
	}
	
	public String printAllCounterAll() {
		
		StringBuffer sb = new StringBuffer(mapCOUNTER.size()+20);		
		sb.append(String.format("{\"counterName\":\"%s\", \"events_count\": [", counterName));
		System.out.println("------------------------------------------------------");
		System.out.println("* Counter Name: "+counterName);
		System.out.println("------------------------------------------------------");
		long totalEventCount = 0;
		long eventCount=0;
		for(String keyVal : mapCOUNTER.keySet()) {
			eventCount=mapCOUNTER.get(keyVal).getCount();
			totalEventCount+=eventCount;
			System.out.printf("* 이벤트 유형: %s - 총 %d개\n", keyVal, eventCount );
			sb.append(String.format("{\"%s\": %d},", keyVal, eventCount));
		}
		System.out.println("------------------------------------------------------");
		System.out.println("* 총 처리 이벤트 : "+totalEventCount+" 개 입니다.");
		System.out.println("------------------------------------------------------");
		String retArrJson = sb.toString().substring(0, sb.toString().lastIndexOf(","));
		retArrJson = retArrJson + "]}";
		return retArrJson;
		 
	}
}
