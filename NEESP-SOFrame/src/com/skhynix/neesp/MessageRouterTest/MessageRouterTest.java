package com.skhynix.neesp.MessageRouterTest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.jms.*;

import com.skhynix.neesp.util.ReceivedTimeoutException;


public class MessageRouterTest {
	
		private static MessageRouterTest instance = new MessageRouterTest();
		public static MessageRouterTest getInstance() { return instance; }

		private static Map<String, EMSMessageRouter> mapMRINFO = new HashMap<>();
		
		private MessageRouterTest() {
			/// 메시지 라우팅 정보 관리
		}
		
		private String getInboundKey(String eqpId){
			return String.format("%s.INBOUND",eqpId);
		}
		
		private String getOutboundKey(String eqpId) {
	   		return String.format("%s.OUTBOUND",eqpId);
	   	}
		
		public void setInOutChannelInfo(String eqpId, EMSMessageRouter inboundEMSChannel, EMSMessageRouter outboundEMSChannel) {
			mapMRINFO.put(getInboundKey(eqpId), inboundEMSChannel);
			mapMRINFO.put(getOutboundKey(eqpId), outboundEMSChannel);
		}		
		
		public String getOutboundDestination(String eqpId) throws Exception{
			return mapMRINFO.get(getOutboundKey(eqpId)).getDestinationName();
		}
		
		public String getInboundDestination(String eqpId) throws Exception {
			return mapMRINFO.get(getInboundKey(eqpId)).getDestinationName();
		}
		
		public void produceMessage(String eqpId, String message) throws Exception{
		 	mapMRINFO.get(getOutboundKey(eqpId)).sendMessage(message);
		}
		
		public String consumeMessage(String eqpId, long waitTimeOut) throws JMSException, ReceivedTimeoutException{		
			if(mapMRINFO.get(getInboundKey(eqpId)) == null){
				return null;
		  	}
		  
		  	return mapMRINFO.get(getInboundKey(eqpId)).receiveMessage(waitTimeOut);
		}
	
}
		