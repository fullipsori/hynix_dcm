package com.skhynix.neesp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.skhynix.neesp.util.*;
import com.skhynix.neesp.log.*;

public class SWWorkerInfo {

	    private String eqpId = "_default";	    
	    private String swnodeId = "_default";
	    private String workerId = "_default";
	    private String workerStatus = "not-yet";
	    private String messageId = "";
	    private String msgProcStage = "not-yet";
	    private long countTimeoutEvent = 0;
	    private long createdTime = 0;
	    private long lastUpdateTime = 0;
	    private String SORequest = "";
	    private String SWRequest = "";
	    
	    private Counter processedEventCounter = null;
	    private NEESPLogger eqpLogger = null;
	    
	    public SWWorkerInfo(String eqpId, String swnodeId) {
	      this.eqpId = eqpId;
	      this.swnodeId = swnodeId;
	      this.workerStatus = "created";
	     
	      processedEventCounter = new Counter(String.format("EventCounter-%s", eqpId));
	      eqpLogger = new NEESPLogger(eqpId, swnodeId,LogManager.getInstance().getLogger());		
			
	      createdTime = System.currentTimeMillis();
	      lastUpdateTime = System.currentTimeMillis();
	    }
	    
	    
	    // 로그 관련 설정 초기화 시점 - Default로 초기화 그리고 
	    public void initializeNEESPLogger() {
	    	eqpLogger = new NEESPLogger(eqpId, swnodeId, LogManager.getInstance().getLogger());
	    }
	    
	    public NEESPLogger getLogger() {
	    	return eqpLogger;
	    }	
	    
	    public void setEqpId(String eqpId) {this.eqpId = eqpId;}
	    public void setSwnodeId(String swnodeId) {this.swnodeId = swnodeId;}
	    public void setWorkerId(String workerId) {this.workerId = workerId;}
        public void setWorkerStatus(String status) {this.workerStatus = status;}
	    public void setMessageId(String messageId) {this.messageId = messageId;}
	    public void setMsgProcStage(String stage) {this.msgProcStage = stage;}
	    public void setLastUpdateTime(long lastUpdateTime) {this.lastUpdateTime = lastUpdateTime;}
	    public void setLastUpdateTime() {this.lastUpdateTime = System.currentTimeMillis();}
	    public void setCountTimeoutEvent(long countTimeoutEvent) {this.countTimeoutEvent = countTimeoutEvent;}
	    
	    // class Request 및 ReqQueue 필요성 여부 체크 
	    public void setSORequest(String request) {this.SORequest = request;}
	    public void setSWRequest(String request) {this.SWRequest = request;}
	    
	    public String getEqpId() {return eqpId;}
	    public String getSwnodeId() {return swnodeId;}
	    public String getWorkerId() {return workerId;}
	    public String getWorkerStatus() {return workerStatus;}
	    public String getMessageId() {return messageId;}
	    public String getMsgProcStage() {return msgProcStage;}
	    public long getLastUpdateTime() {return lastUpdateTime;}
	    public long getCreatedTime() {return createdTime;}
	    
	    public String getSORequest() {return SORequest;}
	    public String getSWRequest() {return SWRequest;}
	    
	    public long getCountTimeoutEvent() {return this.countTimeoutEvent;}
	    public void increaseTimeoutEventCount() { this.countTimeoutEvent++;}
	    
	    public long howLongDoesWorkerWork() { return (System.currentTimeMillis()-this.createdTime);}

	    public void increaseEventCount(String eventType) {
	    	this.processedEventCounter.increaseCount(eventType);
	    }
	    
	    public void restEventCount(String eventType) {
	    	this.processedEventCounter.resetCount(eventType);
	    }
	    
	    public void restEventCountAll() {
	    	this.processedEventCounter.resetCountAll();
	    }
	    
	    public String JsonEventCountVal() {
	    	return this.processedEventCounter.printAllCounterAll();
	    }

	    public String getSWWorkerInfo(String jobTitle) {
	    
	        StringBuffer sb = new StringBuffer(100);
	        sb.append(String.format("======================================================\n"));
            sb.append(String.format("* 작업 내용: %s\n", jobTitle));
	        sb.append(String.format("======================================================\n"));
            sb.append(String.format("* 장비Id: %s\n", eqpId));
            sb.append(String.format("* SWNode Id: %s\n", swnodeId));
            sb.append(String.format("* Worker Id: %s\n", workerId));
            sb.append(String.format("* Worker 상태: %s\n", workerStatus));
            sb.append(String.format("* 메시지 Id: %s\n", messageId));
            sb.append(String.format("* 메시지 처리 단계: %s\n", msgProcStage));
            sb.append(String.format("* SO 요청 명령어: %s\n", SORequest));
            sb.append(String.format("* SW 요청 명령어: %s\n", SWRequest));
            sb.append(String.format("* 타임아웃이벤트 개수: %d\n", countTimeoutEvent));
            sb.append(String.format("======================================================\n"));
	        
	        return sb.toString();      
        }
	    
	    public String printCounterInfo() {
	    	return this.processedEventCounter.printAllCounterAll();
	    }
    
}
