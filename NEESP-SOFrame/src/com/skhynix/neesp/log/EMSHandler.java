package com.skhynix.neesp.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.jms.*;


public class EMSHandler extends Handler {
	
	private static String applicationName  = "LogEMS"; 
	
    /*-----------------------------------------------------------------------
     * Parameters
     *----------------------------------------------------------------------*/
    
	// String          serverUrl    = "localhost:7222";
	String          serverUrl    = "192.168.232.142:7222";
    String          userName     = null;
    String          password     = null;
    String          topicName    = "topic.neesp.log";    
    boolean         useTopic     = true;
    boolean         useAsync     = false;

    /*-----------------------------------------------------------------------
     * Variables
     *----------------------------------------------------------------------*/
    Connection      connection   = null;
    Session         session      = null;
    MessageProducer msgProducer  = null;    
    Destination     destination  = null;
    TextMessage 	msg			 = null;
    
	
	
    private String calcDate(long millisecs) {
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate);
    }
    
    public EMSHandler(String applicationName, String topicName) {
    	
        try 
        {	
        	this.applicationName = applicationName;
        	this.serverUrl = LogManager.getInstance().getLogServerUrlEMS();
        	this.topicName = topicName;
            System.err.println("Publishing to destination '"+topicName+"'\n");
            ConnectionFactory factory = new com.tibco.tibjms.TibjmsConnectionFactory(serverUrl);
            connection = factory.createConnection(userName,password);
            /* create the session */
            session = connection.createSession(javax.jms.Session.AUTO_ACKNOWLEDGE);
            /* create the destination */
            destination = session.createTopic(topicName);
            /* create the producer */
            msgProducer = session.createProducer(null);            
            /* publish messages */
            msg = session.createTextMessage();
        } 
        catch (JMSException e) 
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
		
	@Override
	public void publish(LogRecord record) {		
		int threadId = record.getThreadID();		
	    StringBuilder sb = new StringBuilder();	    
	    sb.append(calcDate(record.getMillis()))
	      .append(" [")
	      .append(this.applicationName)
	      .append("][")
	      .append(record.getSourceClassName())
	      .append("][")
	      .append(record.getSourceMethodName())
	      .append("] ")
	      .append(record.getMessage());	    
	    SendLogToEMS(sb.toString());
	}
	
	public void SendLogToEMS(String logMessage) {
		try {
			msg.setText(logMessage);
		    msgProducer.send(destination, msg);
	    } catch(Exception ex) {
	   		ex.printStackTrace();
	    }
	}
	
	
	@Override
	public void flush() {
		
	}
	
	@Override
	public void close() throws SecurityException {
		try {
			// 관련 문제를 해결한다. - 종료작업을 불러준다.
	        session.close();
	        connection.close();
       }catch(Exception ex) {
       		ex.printStackTrace();
       }
	}

	private long getCurrentTime(){
	   Date date = new Date();
	    return date.getTime();
	}
	
}