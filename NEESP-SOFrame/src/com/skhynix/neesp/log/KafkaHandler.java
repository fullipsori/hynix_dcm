package com.skhynix.neesp.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaHandler extends Handler {
	
	private String logTopic = "quickstart-events";
	private String logServerURL = "192.168.232.142:9192";
	private String appNodeName = "";
	private Properties props = new Properties();
	private Producer<String, String> producer = null;
    private String calcDate(long millisecs) {
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate);
    }
    
    public KafkaHandler(String topicName, String serverURL) {
    	
    	this.logTopic = topicName;
    	this.logServerURL = serverURL;
    	
    	//Assign localhost id
    	props.put("bootstrap.servers", this.logServerURL);		  
    	//Set acknowledgements for producer requests.      
    	props.put("acks", "all");		  
    	//If the request fails, the producer can automatically retry,
    	props.put("retries", 0);		  
		//Specify buffer size in config
		props.put("batch.size", 16384);		  
		//Reduce the no of requests less than 0   
		props.put("linger.ms", 1);		  
		//The buffer.memory controls the total amount of memory available to the producer for buffering.   
		props.put("buffer.memory", 33554432);
		Thread.currentThread().setContextClassLoader(null);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");		     
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		  
		producer = new KafkaProducer<String, String>(props);
    }
		
	@Override
	public void publish(LogRecord record) {
		
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
	    SendLogToKafka(sb.toString());	      
	}
	
	
	@Override
	public void flush() {
		
	}
	
	@Override
	public void close() throws SecurityException {
		this.SendLogToKafka("/// KafakHandler를 종료시키고 정리하겠습니다. ///");
		producer.close();
	}
  
  	int countValue = 0;	
	public void SendLogToKafka(String logMessage) {
		  // create instance for properties to access producer configs		  	    	 
		  producer.send(new ProducerRecord<String, String>(logTopic, "logmanager", logMessage));
		  
	}
}