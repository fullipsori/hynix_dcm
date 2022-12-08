package com.skhynix.neesp.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaHandlerForSWN extends Handler {
	
	private String logTopic = "";
	private String logServerURL = "";
	
	private String appNodeName = "";
	private String eqpID = "";
	
	private Producer<String, String> producer = null;
    private String calcDate(long millisecs) {
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate);
    }
    
    public KafkaHandlerForSWN(String appNodeName, String eqpId, String topicName, Producer<String, String> producer) {
    	
    	System.out.println("KafkaHandlerForSWN ["+producer+"]["+eqpId+"]["+topicName+"]");
    	
    	this.appNodeName = appNodeName;
    	this.eqpID = eqpId;
    	this.logTopic = topicName;
    	this.producer = producer;
    }
		
	@Override
	public void publish(LogRecord record) {
		
	    StringBuilder sb = new StringBuilder();
	    sb.append(calcDate(record.getMillis()))
	      .append(" [")
	      .append(System.nanoTime())
	      .append("][")
	      .append(appNodeName)
	      .append("][")
	      .append(eqpID)
	      .append("][")	      
	      .append(record.getLevel())
	      .append("][")
	      .append(record.getSourceClassName())
	      .append("][")
	      .append(record.getSourceMethodName())
	      .append("] ")
	      .append(record.getMessage());	    
	    SendLogToKafka(sb.toString());	      
	}
	
	
	@Override
	public void flush() {
		
	}
	
	@Override
	public void close() throws SecurityException {
		producer.close();
	}
  
  	int countValue = 0;	
	public void SendLogToKafka(String logMessage) {
		  // create instance for properties to access producer configs		  	    	 
		  producer.send(new ProducerRecord<String, String>(logTopic, eqpID, logMessage));
		  
	}
}