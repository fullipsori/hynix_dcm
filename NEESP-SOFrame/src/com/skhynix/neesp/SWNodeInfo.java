package com.skhynix.neesp;

import java.util.ArrayList;

public class SWNodeInfo {	    
	    private String swnodeId = "";
	    private String swnodeStatus = "";
	    private ArrayList<String> eqpIds = null;
	    private long createdTime = 0;
	    private long lastUpdateTime = 0;
	    
	    public SWNodeInfo(String swnodeId, String status) {	      
	      this.swnodeId = swnodeId;
	      this.swnodeStatus = status;
	      createdTime = System.currentTimeMillis();
	      lastUpdateTime = System.currentTimeMillis();
	      this.eqpIds = new ArrayList<>(); 
	    }
	    
	    public void setSwnodeId(String swnodeId) {this.swnodeId = swnodeId;}
	    public void setSwnodeStatus(String status) {this.swnodeStatus = status;}
	    public void setLastUpdateTime(long lastUpdateTime) {this.lastUpdateTime = lastUpdateTime;}
	    public void setLastUpdateTime() {this.lastUpdateTime = System.currentTimeMillis();}
	    
	    public String getSwnodeId() {return swnodeId;}
	    public String getSwnodeStatus() {return swnodeStatus;}
	    public String getEqpIds() { return eqpIds.toString();}	    
	    public long getLastUpdateTime() {return lastUpdateTime;}
	    public long getCreatedTime() {return createdTime;}	    

	    public void addEqpId(String eqpId) {this.eqpIds.add(eqpId);}
	    public void removeEqpId(String eqpId) { eqpIds.remove(eqpId);}
	    public void clearEqpIds() {this.eqpIds.clear();}
	    
	    public boolean isExitEqpId(String eqpId) {
	    	boolean bRet = false; 
	    	
	    	for(int i=0; i < eqpIds.size(); i++) {
                if(eqpIds.get(i).equals(eqpId)) return true;
	    	}
	    	
	    	return bRet;
	    }
	    
	    public String getSWNodeInfo(String jobTitle) {
	    
	        StringBuffer sb = new StringBuffer(100);
	        sb.append(String.format("======================================================\n"));
            sb.append(String.format("* 작업 내용: %s\n", jobTitle));
	        sb.append(String.format("======================================================\n"));
            sb.append(String.format("* SWNode Id: %s\n", swnodeId));
            sb.append(String.format("* SWNode 상태: %s\n", swnodeStatus));
            sb.append(String.format("* 할당 장비 Ids 목록\n"));
            for(int i=0; i < eqpIds.size(); i++) {
                  sb.append(String.format(" - [%d][%s]\n", i, eqpIds.get(i)));
            }
            sb.append(String.format("======================================================\n"));
	        return sb.toString();      
        }
	    
	    public void printSWNodeInfo(String jobTitle) {
	    	System.out.println(this.getSWNodeInfo(jobTitle));
	    }
	    
}