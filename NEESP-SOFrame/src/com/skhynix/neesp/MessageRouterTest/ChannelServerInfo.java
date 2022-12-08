package com.skhynix.neesp.MessageRouterTest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.jms.*;


public class ChannelServerInfo {
	
	private static String clientName 	  = "LogEMS"; 
	
    /*-----------------------------------------------------------------------
     * Parameters
     *----------------------------------------------------------------------*/
    private String channelType = "INBOUND";
    private String serverType = "";
    private String serverName = "";
    private String serverUrl    = "192.168.232.142:7222";
    private String destinationType = "TOPIC";
    private String destinationName = ""; // QUEUE.FAB1.ZONE1.PHOTO.EQP1
    private String userName     = null;    
    private String password     = null;
    private int    ackMode 		= javax.jms.Session.AUTO_ACKNOWLEDGE;
    private String strAckMode	= "AUTO_ACK";

    /*-----------------------------------------------------------------------
     * Variables
     *----------------------------------------------------------------------*/
    Connection      connection   = null;
    Session         session      = null;    	
	
    public ChannelServerInfo(String[] channels) throws JMSException {

			this.channelType = channels[0];
			this.serverType = channels[1];
			this.serverName = channels[2];
			this.serverUrl = channels[3];			
			this.strAckMode = channels[5];
			
			
			System.err.printf("1. [%s][%s][%s] 목적지 유형 설정 후 내용 확인 [%s]\n", this.channelType, this.serverType, this.serverName, this.serverUrl);
			initializeConnection();
    }
    
    public void setAckMode(int sessionAckMode){ this.ackMode = sessionAckMode; }
    
    public String[] initializeConnection() throws JMSException {
    
    	    String[] retVals = {"",""};

            System.err.println("* ChannelServerInfo - Publishing to destination '"+destinationName+"'");
            ConnectionFactory factory = new com.tibco.tibjms.TibjmsConnectionFactory(serverUrl);
            connection = factory.createConnection(userName,password);
            /* create the session */
            session = connection.createSession(this.ackMode);
            
            retVals[0] = "succeed-create-new-seesion";
            retVals[1] = String.format("정상적으로 [%s][%s][%s] 세션이 형성되었습니다.￦n", this.serverName, this.serverUrl, this.serverType);
            
            connection.start();
            
            return retVals;
    }
    
    public String getServerName(){ return this.serverName; }
    public String getChannelType() { return this.channelType; }
    public String getAckMode() { return String.format("%d",this.ackMode); } 
    public Session getSession() { return this.session; } // EMS Session 정보 넘겨주기    
    
    // 채널의 키는 서버정보 관리자 내에서 유일무이해야 한다. 
    public String getChannelKey(){ return String.format("%s.%s.%d", this.serverName, this.channelType, this.strAckMode); }    
    
    
    public void close() throws JMSException {
    		session.close();
    		connection.close();
    }
}