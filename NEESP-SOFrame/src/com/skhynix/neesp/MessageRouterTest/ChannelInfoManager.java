package com.skhynix.neesp.MessageRouterTest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.jms.*;


public class ChannelInfoManager {
	
		private static ChannelInfoManager instance = new ChannelInfoManager();
		public static ChannelInfoManager getInstance() { return instance; }

		private static Map<String, ChannelServerInfo> mapCHANNELSERVERINFO = new HashMap<>();
		
		private ChannelInfoManager() {
			/// 채널 서버 정보 관리자 생성자
		}
		
		public void setChannelServerInfo(ChannelServerInfo channelSI) {
			if(!mapCHANNELSERVERINFO.containsKey(channelSI.getChannelKey())) {
				mapCHANNELSERVERINFO.put(channelSI.getChannelKey(), channelSI);
				System.err.printf("[%s] ChannelInfoManager에 등록하였습니다. ￦n", channelSI.getChannelKey());
			} else {
				System.err.printf("[%s] 이미 등록되어 있는 채널 서버입니다..￦n", channelSI.getChannelKey());
			}
		}
		
		public Session getSession(String channelKey) throws Exception {
			return mapCHANNELSERVERINFO.get(channelKey).getSession();
		}
}
		
			