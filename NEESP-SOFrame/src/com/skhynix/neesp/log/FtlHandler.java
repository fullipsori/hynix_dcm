package com.skhynix.neesp.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.tibco.ftl.FTL;
import com.tibco.ftl.Publisher;
import com.tibco.ftl.Realm;
import com.tibco.ftl.TibProperties;


public class FtlHandler extends Handler {
	
	private static String clientName 	  = "LogFtl"; 
	private static String realmService    = "192.168.232.142:8585";
	private static String applicationName = "default";
	private static String endpointName    = "default";
    private static Publisher pub = null;
    private static com.tibco.ftl.Message msg = null;
    private static Realm realm = null;
    private static String logType = "hello";
	
	
    private String calcDate(long millisecs) {
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate);
    }
    
    public FtlHandler(String logName, String RealmService, String ApplicationName, String EndpointName, String LogType) {
    	
        realmService    = RealmService;
        applicationName = ApplicationName;
        endpointName    = EndpointName;
        logType 		= LogType;

        clientName = getUniqueName(logName);
        /*
         * A properties object (TibProperties) allows the client application to set attributes (or properties) for an object
         * when the object is created. In this case, set the client label property (PROPERTY_STRING_CLIENT_LABEL) for the 
         * realm connection. 
         */
        try {
        	
        	System.out.println("1");
	        TibProperties   props = null;
	        props = FTL.createProperties();
	        props.set(Realm.PROPERTY_STRING_CLIENT_LABEL, clientName);
	 
	        /*
	         * Establish a connection to the realm service. Specify a string containing the realm service URL, and 
	         * the application name. The last argument is the properties object, which was created above.
	        
	         * Note the application name (the value contained in applicationName, which is "default"). This is the default application,
	         * provided automatically by the realm service. It could also be specified as null - it mean the same thing,
	         * the default application. 
	         */
	        System.out.println("2");
	        realm = FTL.connectToRealmServer(realmService, applicationName, props);
	     
	        /* 
	         * It is good practice to clean up objects when they are no longer needed. Since the realm properties object is no
	         * longer needed, destroy it now. 
	         */
	        System.out.println("3");
	        props.destroy();
	
	        /*
	         * Before a message can be published, the publisher must be created on the previously-created realm. Pass the endpoint 
	         * onto which the messages will be published.
	
	         * In the same manner as the application name above, the value of endpointName ("default") specifies the default endpoint,
	         * provided automatically by the realm service.
	         */
	        System.out.println("4");
	        pub = realm.createPublisher(endpointName);   
	
	        /* 
	         * In order to send a message, the message object must first be created via a call to createMessage() on realm. Pass the
	         * the format name. Here the format "helloworld" is used. This is a dynamic format, which means it is not defined in the 
	         * realm configuration. Dynamic formats allow for greater flexibility in that changes to the format only require changes 
	         * to the client applications using it, rather than an administrative change to the realm configuration.
	         */
	        System.out.println("4");
	        msg = realm.createMessage("helloworld");
	
	        /*
	         * A newly-created message is empty - it contains no fields. The next step is to add one or more fields to the message.
	         * setString() method call on previously-created message adds a string field. First, a string field named "type" is added,
	         * with the value "hello".Then a string field named "message" is added with the value "hello world earth".
	         */
	        	
	        /* 
	         * Once the message is complete, it can be sent via send() method call on previously-created publisher. Pass the message 
	         * to be sent.
	         */
	
        }catch(Exception ex) {
        	ex.printStackTrace();
        }
    }
		
	@Override
	public void publish(LogRecord record) {
		
		int threadId = record.getThreadID();
		
	    StringBuilder sb = new StringBuilder();
	    sb.append(calcDate(record.getMillis()))
	      .append(" [")
	      .append(System.nanoTime())
	      .append("][")
	      .append(record.getLevel())
	      .append("][")
	      .append(record.getSourceClassName())
	      .append("][")
	      .append(record.getSourceMethodName())
	      .append("] ")
	      .append(record.getMessage());	    
	    
	    SendLogToFtl(sb.toString());	      
	}
	
	public void SendLogToFtl(String logMessage) {
		try {
			msg.setString("type", logType);
			msg.setString("message", logMessage);	            
			pub.send(msg);
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
	        // Once objects are no longer needed, they should be disposed of - and generally in reverse order of creation.       
	        // First destroy the message.
	        msg.destroy();       
	        // Then Close the Publisher
	        pub.close();
	        // Next close the connection to the realm service.
	        realm.close();	        
       }catch(Exception ex) {
       		ex.printStackTrace();
       }
	}	
	
	// FTL 메시지 보내기 위한 연관 함수들 및 ftlMessageSender 
    private String getUniqueName(String sampleName) {   
	    if (sampleName != null && !sampleName.isEmpty()){
	        return sampleName + "_" + getCurrentTime();
	    }
	    return "";
	}

	private long getCurrentTime(){
	   Date date = new Date();
	    return date.getTime();
	}
	
}