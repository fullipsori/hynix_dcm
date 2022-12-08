package com.skhynix.neesp.MessageRouterTest;

import javax.jms.*;

import com.skhynix.neesp.util.ReceivedTimeoutException;
import com.skhynix.neesp.util.bwReturnValues;

public class EMSMessageRouter {
	
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
    private int    akcMode = javax.jms.Session.AUTO_ACKNOWLEDGE;

    /*-----------------------------------------------------------------------
     * Variables
     *----------------------------------------------------------------------*/
    Connection      connection   = null;
    Session         session      = null;
    MessageProducer msgProducer  = null;
    MessageConsumer msgConsumer  = null;   
    Destination     destination  = null;
    TextMessage 	msg			 = null;
    
    String		    channelKey   = "";
    String			strAckMode   = "AUTO_ACK";
	
    public EMSMessageRouter(String[] channels) throws Exception {
    	
		this.channelType = channels[0];
		this.serverType = channels[1];
		this.serverName = channels[2];
		this.serverUrl = channels[3];
		this.destinationName = channels[4];
		this.strAckMode = channels[5];
		
		this.destinationType = destinationName.substring(0, destinationName.indexOf("."));
		System.err.printf("2. 목적지 유형 설정 후 내용 확인 [%s]￦n", this.destinationName);
		
		this.channelKey = String.format("%s.%s.%d", this.serverName, this.channelType, this.strAckMode);
		this.session = ChannelInfoManager.getInstance().getSession(channelKey);
		
		establishConnectionWithDestination(session);
		
    }
    
    public String[] establishConnectionWithDestination(Session session) throws Exception {
	 	
        System.err.println("/// Publishing to destination '"+this.destinationName+"'\n");
        
        /* create the destination */
        if(this.destinationType.equals("QUEUE")){
            destination = session.createQueue(this.destinationName);
        } else if(this.destinationType.equals("TOPIC")){	            
        	destination = session.createTopic(this.destinationName);
    	}
        
        /* create the producer */
        if(this.channelType.equals("INBOUND")){ 
        	msgConsumer = session.createConsumer(this.destination);            
        } else if(this.channelType.equals("OUTBOUND")) {
        	msgProducer = session.createProducer(null);            
        }
        
        /* consumer/publish messages */
        msg = session.createTextMessage();
        
        String strRet = String.format("[%s] 정상적으로 [%s][%s][%s][%s] 세션을 생성하였습니다.",
                                       this.channelType, this.serverName, this.serverUrl, this.destinationName, this.destinationType);
        
        return new bwReturnValues().retVal1("succeed-create-new-session", strRet);
    }
    
    public MessageProducer getMessageProducer(){ return msgProducer; }
    public MessageConsumer getMessageConsumer(){ return msgConsumer; }
    
    public String getDestinationName() {return this.destinationName; }
    public String getChannelType() {return this.channelType; }
    
    public String receiveMessage(long waitTimeOut) throws JMSException, ReceivedTimeoutException {    	
		/*
		 * 타임아웃에 의해서 메시지를 가져오지 못한 경우를 처리하기 위한 것
		 */
		msg = (TextMessage)msgConsumer.receive(waitTimeOut);
		
		if(msg == null) {
			throw new ReceivedTimeoutException(String.format("대기시간 [%d ms] 타임아웃 이벤트가 발생하였습니다.", waitTimeOut));
		}
		return msg.getBody(String.class);
 	}
    
    public void sendMessage(String message) throws JMSException, Exception {
    	msg.setText(message);
    	msgProducer.send(msg);
    }
}