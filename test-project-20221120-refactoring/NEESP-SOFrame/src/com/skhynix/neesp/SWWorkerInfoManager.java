package com.skhynix.neesp;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SWWorkerInfoManager {

    private static Logger logger = Logger.getLogger(SWWorkerInfoManager.class.getName());
    
    private static SWWorkerInfoManager instance = new SWWorkerInfoManager();
    public static SWWorkerInfoManager getInstance() {return instance;}
    
    private Map<String, SWWorkerInfo> mapSWWORKERINFO = null;
    
    public SWWorkerInfoManager() {
        mapSWWORKERINFO = new HashMap<>();
        System.out.printf("/// SWWorkerInfoManager 생성자\n");
    }
    
    public boolean initSWWorkerInfo(String eqpId, String swnodeId) {
    
        if(mapSWWORKERINFO.containsKey(eqpId)) {
            System.out.printf("[%s] 이미 SW-worker 있습니다.", eqpId); 
            // 상태 정보에 따라서 처리 방식 결정
            return false;
        } else {
            mapSWWORKERINFO.put(eqpId, new SWWorkerInfo(eqpId, swnodeId));
            System.out.printf("/// 새롭게 [%s] SW-worker 있습니다.", eqpId, swnodeId);
            return true;
        }
    }
    
    public String[] releaseSWWorkerInfo(String eqpId) {    	
    	String[] retVals = {"",""};
    	
    	if(mapSWWORKERINFO.containsKey(eqpId)) {
            // 상태 정보에 따라서 처리 방식 결정
            String logMsg = String.format("[%s] 장비관련 Worker 정보를 가져옵니다. SWorkerInfo를 제거합니다.", eqpId);
            logger.log(Level.INFO, logMsg);
            System.out.println(logMsg);                        
            mapSWWORKERINFO.remove(eqpId);
            retVals[0] = "ok-release-swworker";
            retVals[1] = "";
        } else {
            // 오류에 따른 처리 진행
            System.out.printf("[%s]장비를 위한 SW-Worker가 없습니다.\n", eqpId);
            retVals[0] = "error-not-found-swworker";
            retVals[1] = String.format("[%s] 장비를 찾을 수 없습니다.",eqpId);
        }
    	
    	return retVals;
    }
    
    public SWWorkerInfo getSWWorkerInfo(String eqpId) {
        
        if(mapSWWORKERINFO.containsKey(eqpId)) {
            // 상태 정보에 따라서 처리 방식 결정
            String logMsg = String.format("[%s] 장비관련 Worker 정보를 가져옵니다.", eqpId);
            // System.out.println(logMsg);                        
            return mapSWWORKERINFO.get(eqpId);
        } else {
            // 오류에 따른 처리 진행
            System.out.printf("[%s]장비를 위한 SW-Worker가 없습니다.\n", eqpId); 
        }
        
        return null;
    }
    

    public void setSWNodeId(String eqpId, String allocatedSWNodeId) {
        
        if(mapSWWORKERINFO.containsKey(eqpId)) {
            // 상태 정보에 따라서 처리 방식 결정
            String logMsg = String.format("할당 받은 [%s] SW-NODE ID를 설정합니다. ", allocatedSWNodeId);
            mapSWWORKERINFO.get(eqpId).setSwnodeId(allocatedSWNodeId);
            System.out.println(logMsg);                        
            
        } else {
            // 오류에 따른 처리 진행
            System.out.printf("[%s] 장비를 위한 SW-Worker가 없습니다.\n", eqpId); 
        }
        
    }

    
    public boolean changeSWWorkerInfo(String eqpId, String appNodeName, String procInstanceId, String workerStatus, String jobTitle) {
        
    	String swnodeId = "";
    	String workerId = "";
    	
        if(mapSWWORKERINFO.containsKey(eqpId)) {
            if(appNodeName == "") swnodeId = "n/a";
            if(procInstanceId == "") workerId = "n/a";            
            
            String logMsg = String.format("[%s] 장비 값을 설정합니다. [작업제목:%s] ", eqpId, jobTitle);
            System.out.println(logMsg);
            mapSWWORKERINFO.get(eqpId).setSwnodeId(swnodeId);
            mapSWWORKERINFO.get(eqpId).setWorkerId(workerId);
            mapSWWORKERINFO.get(eqpId).setWorkerStatus(workerStatus);
            
            mapSWWORKERINFO.get(eqpId).setLastUpdateTime();
                                    
            return true;
        } else {
            // 오류에 따른 처리 진행
            System.out.printf("[%s] 존재하지 않는 장비 입니다.", eqpId); 
            return false;
        }
              
    }
    
    public void printSWWorkerInfo(String eqpId, String jobTitle) {
    
        if(mapSWWORKERINFO.containsKey(eqpId)) {
        	System.out.printf("[%s] 장비 정보를 출력하겠습니다. [작업제목:%s]\n", eqpId, jobTitle);
            String logMsg = mapSWWORKERINFO.get(eqpId).getSWWorkerInfo(jobTitle);
            logger.log(Level.INFO, logMsg);
            System.out.println(logMsg);
        } else {
            // 오류에 따른 처리 진행
            System.out.printf("[%s] 존재하지 않는 장비 입니다. [작업제목:%s]\n", eqpId, jobTitle); 
        }
    }

}
