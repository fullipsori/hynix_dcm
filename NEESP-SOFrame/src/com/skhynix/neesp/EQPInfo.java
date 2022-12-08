package com.skhynix.neesp;

import java.util.ArrayList;

public class EQPInfo {

	    private String eqpId = "";
	    private String eqpStatus = "";
	    private String swnodeId = "";
	    private String workerId = "";
	    private String workerStatus = "";
	    private String upperEqpId = "";
	    private ArrayList<String> siblingEqpIds = new ArrayList<String>();
	    private String inboundQueue = "";
	    private String outboundQueue = "";
	    private long createdTime = 0;
	    private long lastUpdateTime = 0;
	    private long countTimeoutEvent = 0;
	    
	    public EQPInfo(String eqpId, String inboundQueue, String status) {
	      this.eqpId = eqpId;
	      this.eqpStatus = status;
	      this.workerStatus = "not-assigned";
	      this.inboundQueue = inboundQueue;
	      createdTime = System.currentTimeMillis();
	      lastUpdateTime = System.currentTimeMillis();
	    }
	    
	    public void setEqpId(String eqpId) {this.eqpId = eqpId;}
	    public void setEqpStatus(String status) {this.eqpStatus = status;}
	    public void setSwnodeId(String swnodeId) {this.swnodeId = swnodeId;}
	    public void setWorkerId(String workerId) {this.workerId = workerId;}
	    public void setWorkerStatus(String workerStatus) {this.workerStatus = workerStatus;}
	    public void setUpperEqpId(String upperEqpId) {this.upperEqpId = upperEqpId;}
	    public void setInboundQueue(String inboundQueue) {this.inboundQueue = inboundQueue; }
	    public void setOutboundQueue(String outboundQueue) {this.outboundQueue = outboundQueue; }
	    public void setSiblingEqpId(String siblingEqpId) {this.siblingEqpIds.add(siblingEqpId);}
	    public void setLastUpdateTime(long lastUpdateTime) {this.lastUpdateTime = lastUpdateTime;}
	    public void setLastUpdateTime() {this.lastUpdateTime = System.currentTimeMillis();}
	    public void setCountTimeoutEvent(long countTimeoutEvent) {this.countTimeoutEvent = countTimeoutEvent;}
	    
	    public String getEqpId() {return eqpId;}
	    public String getEqpStatus() {return eqpStatus;}
	    public String getSwnodeId() {return swnodeId;}
	    public String getWorkerId() {return workerId;}
	    public String getWorkerStatus() {return workerStatus;}
	    public String getUpperEqpId() {return upperEqpId;}
	    public String getInboundQueue() {return inboundQueue;}
	    public String getOutboundQueue() {return outboundQueue;}
	    public String getSiblingEqpIds() {return siblingEqpIds.toString();}
	    public long getLastUpdateTime() {return lastUpdateTime;}
	    public long getCreatedTime() {return createdTime;}
	    
	    public long getCountTimeoutEvent() {return this.countTimeoutEvent;}
	    public void increaseTimeoutEventCount() { this.countTimeoutEvent++;}
	    
	    public void removeSiblingEqpId(String siblingEqpId) {siblingEqpIds.remove(siblingEqpId);}
	    
	    public String getEQPInfo(String jobTitle) {
	    
	        StringBuffer sb = new StringBuffer(100);
	        sb.append(String.format("======================================================\n"));
            sb.append(String.format("* 작업 내용: %s\n", jobTitle));
	        sb.append(String.format("======================================================\n"));
            sb.append(String.format("* 장비Id: %s\n", eqpId));
            sb.append(String.format("* 장비 상태: %s\n", eqpStatus));
            sb.append(String.format("* SWNode Id: %s\n", swnodeId));
            sb.append(String.format("* Worker Id: %s\n", workerId));
            sb.append(String.format("* Worker 상태: %s\n", workerStatus));
            sb.append(String.format("* upper 장비Id: %s\n", upperEqpId));
            sb.append(String.format("* 종속 장비Id: %s\n", siblingEqpIds.toString()));
            sb.append(String.format("* Inbound Queue명: %s\n", inboundQueue));
            sb.append(String.format("* Outbound Queue명: %s\n", outboundQueue));
            sb.append(String.format("* 타임아웃이벤트 개수: %d\n", countTimeoutEvent));
	        sb.append(String.format("======================================================"));
	        
	        return sb.toString();      
        }
	    
	    public String toJson() {
	    	StringBuffer sb = new StringBuffer(100);
	    	sb.append("{");
	    	sb.append(String.format("\"eqpId\": \"%s\",", eqpId));
	    	sb.append(String.format("\"eqpStatus\": \"%s\",", eqpStatus));
	    	sb.append(String.format("\"swnodeId\": \"%s\",", swnodeId));
	    	sb.append(String.format("\"workerId\": \"%s\",", workerId));
	    	sb.append(String.format("\"workerStatus\": \"%s\",", workerStatus));
	    	sb.append(String.format("\"uppderEqpId\": \"%s\",", upperEqpId));
	    	sb.append(String.format("\"sbilingEqpIds\": \"%s\",", siblingEqpIds.toString()));
	    	sb.append(String.format("\"inboundQueue\": \"%s\",", inboundQueue));
	    	sb.append(String.format("\"outboundQueue\": \"%s\",", outboundQueue));
	    	sb.append(String.format("\"countTimeoutEvent\": \"%s\"", countTimeoutEvent));
	    	sb.append("}");
	    	return sb.toString();
	    }
}
