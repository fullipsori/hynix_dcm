package com.skhynix.neesp.log;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.*;
import java.util.Queue;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;   

public class LogManager {
	
	private static LogManager instance = new LogManager();
	public static LogManager getInstance() { 
		return instance; 
	}
	
	private String applicationName = "";
	private ExecutorService LogQueueMonitor = null; 
	private final Logger mLogger = Logger.getGlobal();
	
	private Handler OutputHandler = new ConsoleHandler();
	private Handler KafkaHandler = null;
	private Handler EmsHandler = null;
	private LEVEL mLevel = LEVEL.INFO;
	
	private long prevMillTime = System.currentTimeMillis();
	private long prevNanoTime = System.nanoTime();
	
	private Producer<String, String> producer = null;
	private String logTopic = "quickstart-events";
	private String logServerURL = "192.168.232.142:9192";	
	private String hostName = "localhost";
	
	private String logTopicEMS = "topic.neesp.log";
	private String logServerUrlEMS = "tcp://192.168.232.142:7222";
	
	// Log Publisher를 이용한 비동기방식 로그 보내기
	private ConcurrentLinkedQueue<String> asyncLogQueue = new ConcurrentLinkedQueue();
	
	private long LP_MONITOR_DELAY = 5;
	private long LP_THREAD_INTERVAL_SEC = 1;
	private long LP_LOGPUB_MAX_LIMIT = 10000; // 초당 최대 1,000개 보내기
	private boolean bStarted = false;
	
	class LogPublisher implements Runnable {
		@Override
        public void run() {
			
        	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
    	   	LocalDateTime now = LocalDateTime.now();
    	   	int currSecond = now.getSecond();
    	   	long count = 1;
    	   	String log = "";
    	   	
    	   	if(currSecond==0) System.out.printf("/////////// 로그 퍼블리싱 모니터: %s [현재: %d초] [큐 로그: %d개]\n", dtf.format(now), currSecond, asyncLogQueue.size());    	   	
    	   	/*
    	   	 * ConcurrentLinkedQueue에 적재된 내용이 있으면 로그를 보낸다.
    	   	 */
    	   	while((log=asyncLogQueue.poll()) != null && count <= LP_LOGPUB_MAX_LIMIT) {
    	   		System.out.println(String.format("[%04d] %s", count, log));
    	   		mLogger.log(Level.INFO, String.format("[%04d] %s", count, log));
    	   		count++;
    	   	}
        }
	}
	
	public void addLog(String log) {
		asyncLogQueue.add(log);
	}
	
	public String getLog() {
		return asyncLogQueue.poll();
	}
	
	public void startMonitor() {
    	((ScheduledExecutorService) LogQueueMonitor).scheduleAtFixedRate(
                new LogPublisher(),
                LP_MONITOR_DELAY,
                LP_THREAD_INTERVAL_SEC,
                TimeUnit.SECONDS
        );
        bStarted = true;
        System.out.printf("//// Log Publishing을 위하여 큐 모니터링을 시작합니다. [쓰레드 체크 간격: %d][%s]\n", LP_THREAD_INTERVAL_SEC, bStarted);
    }
	
	public void setLogServerInfo(String serverUrl, String topicName) {
		this.logServerUrlEMS = serverUrl;
		this.logTopicEMS = topicName;
	}
		
	private class CustomLogFormatter extends Formatter {
	    
	    public String format(LogRecord rec) {
	        StringBuffer buf = new StringBuffer(1000);
	        buf.append(calcDate(rec.getMillis()));
	        
	        buf.append(" [");
	        buf.append(rec.getLevel());
	        buf.append("] ");
	        
	        /**
	        buf.append("[");
	        buf.append(rec.getSourceMethodName());
	        buf.append("] ");
	        **/
	        
	        buf.append(rec.getMessage().trim());
	        buf.append("\n");
	        
	        return buf.toString();
	    }
	    
	    private String calcDate(long millisecs) {
	        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
	        Date resultdate = new Date(millisecs);
	        return date_format.format(resultdate);
	    }
	}

	
	public Handler getKafkaHandler(String appNodeName, String eqpId, String topicName) {
		if(topicName.isEmpty() || topicName == null) {
			System.out.println("LogManager.getKafkaHandler(). ["+producer+"]");
			// return new KafkaHandlerForSWN(appNodeName, eqpId, logTopic, producer);
			return new KafkaHandler(logTopic, logServerURL);
		} else {
			// return new KafkaHandlerForSWN(appNodeName, eqpId, topicName, producer);
			return new KafkaHandler(topicName, logServerURL);
		}
	}

	public LogManager() {		
		System.out.println("/// Log Manager 초기화 작업을 수행합니다."	+ "");
		LogQueueMonitor = Executors.newScheduledThreadPool(1);
	}
	
	public void initializeEMSHandler(String applicationName, String serverUrl, String topicName) {
		this.applicationName = applicationName;
		this.logServerUrlEMS = serverUrl;
		this.logTopicEMS = topicName;
		this.EmsHandler = new EMSHandler(applicationName, this.logTopicEMS);
		// mLogger.addHandler(this.EmsHandler);
		System.out.println("["+this.applicationName+"] 33-initializeEMSHandler ["+this.EmsHandler+"]");
	}
	
	public Handler getEmsLogHandler () {
		System.out.println("44-getEmsLogHandler ["+this.EmsHandler+"]");
		if(this.EmsHandler != null) 
			System.out.println("["+this.applicationName+"] 44-1-getEmsLogHandler 값이 있습니다.");
		else 
			System.out.println("["+this.applicationName+"] 44-2-EMS Handler가 공교롭게도 NULL값을 가지고 있습니다. ["+this.EmsHandler+"]");
		
		return this.EmsHandler; 
	}
	public String getLogServerUrlEMS() { return this.logServerUrlEMS; }
	
	public void initialize(String appNodeName) {
		
		// Activator=>OnStartUp Event에서 초기화 작업 수행
		initializeKafka();
		
		mLevel = LEVEL.INFO;
		mLevel.apply(mLogger);
		OutputHandler.setFormatter(new CustomLogFormatter());
		mLogger.addHandler(OutputHandler);		
	}
	
	public void deinitliaze() {
		if(this.KafkaHandler != null) {
			this.KafkaHandler.close();			
		}
		if(this.EmsHandler != null) {
			this.EmsHandler.close();
			System.err.println("/// EMSHandler Deinitialize");
		}
		this.removeAllHandler();
	}
	
	public void initializeKafka() {
		// 카프카 연결객체 생성하기		
		Properties props = new Properties();
    	//Assign localhost id
    	props.put("bootstrap.servers", logServerURL);		  
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
	
	public LEVEL getLevel() {
		return mLevel;
	}
	
	public void removeAllHandler() {		
		Handler[] hd = mLogger.getHandlers();
		for(int i=0; i > hd.length; i++) {
			if(hd[i] != null) mLogger.removeHandler(hd[i]);
		}
	}

	public void setLevel(LEVEL level) {
		if(level == null) return;
		mLevel = level; // LEVEL.getLevel(level);
		mLevel.apply(mLogger);
	}
	
	public Logger getLogger() {
		return mLogger;
	}
	
	public void setOutputMode(String targetMOM, String topicName, String serverUrl) {
		
	}
	
	public void setOutputMode(boolean fileMode, String filename) {
		try{
			if(fileMode) {
				Handler outHandler = new FileHandler(filename, true);
				// mLogger.removeHandler(OutputHandler);
				OutputHandler = outHandler;
				OutputHandler.setFormatter(new CustomLogFormatter());
				mLogger.addHandler(OutputHandler);
				
			}else {
				if(!(OutputHandler instanceof ConsoleHandler)) {
					mLogger.removeHandler(OutputHandler);
					OutputHandler = new ConsoleHandler();
					OutputHandler.setFormatter(new CustomLogFormatter());
					mLogger.addHandler(OutputHandler);
				}
			}
		}catch(Exception e) {
			System.out.println("Exception:" + e.getMessage());
		}
	}

	public void error(String msg) {
		mLogger.log(Level.SEVERE, msg);
	}
	
	public void warn(String msg) {
		mLogger.log(Level.WARNING, msg);
	}
	
	public void info(String msg) {
		mLogger.log(Level.INFO, msg);
	}
	
	public void debug(String msg) {
		mLogger.log(Level.FINE, msg);
	}
	
}
