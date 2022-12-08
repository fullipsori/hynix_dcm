package com.skhynix.neesp.ConfigManager;

public class ConfigManager {
	
	private static ConfigManager instance = new ConfigManager();
	public static ConfigManager getInstance() { return instance; }
	private ConfigManager() {}
	
	/*
	 * 임시로 테스트를 위하여 만든 설정 관리자
	 */
	public String allocateChannelInformation(String eqpId, String swnodeId, String queueName, String strAckMode) {
		
		/*
		 * eqpId, swnodeId 기반으로 Message Routing에 필요한 기본 정보를 가져온다.
		 * 유기적으로 사용량 기반으로 Channel을 할당하기 위해서는 Intelligent-SO: 지능형
		 */
		
		return String.format("INBOUND,EMS,ems#01,tcp://192.168.232.142:7222,%s,%s|OUTBOUND,EMS,ems@02,tcp://192.168.232.142:7223,%s,%s", 
				queueName, strAckMode, queueName, strAckMode);
	}
}
