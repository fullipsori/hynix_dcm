package com.skhynix.neesp;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EQPInfoManager {
	
    private static Logger logger = Logger.getLogger(EQPInfoManager.class.getName());
    
    private static EQPInfoManager instance = new EQPInfoManager();
    public static EQPInfoManager getInstance() {return instance;}
    
    private Map<String, EQPInfo> mapEQPINFO = null;
    
    public EQPInfoManager() {
        mapEQPINFO = new HashMap<>();
        System.out.printf("/// EQPInfoManager 생성자\n");
    }
    
    public EQPInfo getEQPInfo(String eqpId, String reqCommand) {
        if(mapEQPINFO.containsKey(eqpId)) {
            System.out.printf("[%s] 해당 장비 관련 정보를 설정합니다. [%s]", eqpId, reqCommand); 
            // 상태 정보에 따라서 처리 방식 결정
            return mapEQPINFO.get(eqpId);
        }
        return null;
    }
    
    public String[] initEQPInfo(String eqpId) {
        String[] retVals = {"",""};
        if(mapEQPINFO.containsKey(eqpId)) {
            System.out.printf("[%s] 이미 존재하는 장비 입니다.", eqpId); 
            // 상태 정보에 따라서 처리 방식 결정
            retVals[0]= "error-already-exist";
            retVals[1]= String.format("[%s] 해당하는 장비는 이미 존재합니다. 초기화 할 수 없습니다.",eqpId);
        } else {
            mapEQPINFO.put(eqpId, new EQPInfo(eqpId, "ready"));
            retVals[0]= "ok-added";
            retVals[1]= String.format("[%s] 장비를 정상적으로 초기화 하였습니다.",eqpId);            
            
        }
        return retVals;
    }   
    
    public String[] setSWNodeId(String eqpId, String allocatedSWNodeId, String eqpStatus) {
        String[] retVals = {"",""};
                
        if(mapEQPINFO.containsKey(eqpId)) {
            // 상태 정보에 따라서 처리 방식 결정
            mapEQPINFO.get(eqpId).setSwnodeId(allocatedSWNodeId);
            mapEQPINFO.get(eqpId).setEqpStatus(eqpStatus);
            String logMsg = String.format("[%s]가 [%s] 노드에 할당되었습니다. [%s]", eqpId, allocatedSWNodeId, eqpStatus);
            logger.log(Level.INFO, logMsg);
            System.out.println(logMsg);
            retVals[0]= "ok-set-swnodeid";
            retVals[1]= String.format("[%s][%s] 장비를 위한 SW Node를 설정하였습니다.",eqpId, allocatedSWNodeId);
            return retVals;
        } else {
            // 오류에 따른 처리 진행
            System.out.printf("[%s] 존재하지 않는 장비 입니다.\n", eqpId); 
            retVals[0]= "error-eqp-not-found";
            retVals[1]= String.format("[%s][%s] 해당하는 장비를 찾을 수 없습니다.",eqpId, eqpStatus);
            return retVals;
        }
              
    }
    
    public String[] changeEQPInfo(String eqpId, String swnodeId, String workerId, String eqpStatus, String workerStatus, String jobTitle) {
        String[] retVals = {"",""};        
        if(mapEQPINFO.containsKey(eqpId)) {
            // 상태 정보에 따라서 처리 방식 결정
            mapEQPINFO.get(eqpId).setSwnodeId(swnodeId);
            mapEQPINFO.get(eqpId).setEqpStatus(eqpStatus);
            mapEQPINFO.get(eqpId).setWorkerId(workerId);
            mapEQPINFO.get(eqpId).setWorkerId(workerStatus);
            
            retVals[0]= "ok-changed-eqp-info";
            retVals[1]= String.format("[%s][%s] 장비 정보를 변경하였습니다. [작업명: %s] ",eqpId, eqpStatus, jobTitle);
            
            logger.log(Level.INFO, retVals[1]);
            System.out.println(retVals[1]);                        
            return retVals;
        } else {
            // 오류에 따른 처리 진행
            retVals[0]= "error-eqp-not-found";
            retVals[1]= String.format("[%s] 해당 장비를 찾을 수 없습니다. [작업명: %s]",eqpId, jobTitle);            
            System.out.println(retVals[1]); 
            return retVals;
        }     
    }
    
    public String[] changeEQPInfoWhenCreated(String eqpId, String swnodeId, String workerId, String eqpStatus, String workerStatus) {
        return changeEQPInfo(eqpId, swnodeId, workerId, eqpStatus, workerStatus, "change-info-when-created");
    }
    
    public String[] changeEQPInfoWhenReleased(String eqpId, String swnodeId, String workerId, String eqpStatus, String workerStatus) {
        return changeEQPInfo(eqpId, swnodeId, workerId, eqpStatus, workerStatus, "change-info-when-released");
    }
    
    public void printEQPInfo(String eqpId, String jobTitle) {
    
    	System.out.printf("/// %s %s\n", eqpId, jobTitle);
        if(mapEQPINFO.containsKey(eqpId)) {
            String eqpInfo = mapEQPINFO.get(eqpId).getEQPInfo(jobTitle);
            // logger.log(Level.INFO, eqpInfo);
            System.out.println(eqpInfo);
        } else {
            // 오류에 따른 처리 진행
            System.out.printf("[%s] 존재하지 않는 장비 입니다. [작업제목:%s]", eqpId, jobTitle); 
        }
    }
    
    public void printAllEQPInfo() {    	
    	Set<String> keySet = mapEQPINFO.keySet();
    	for(String eqpId : keySet) {
    		printEQPInfo(eqpId,"정보 조회");
    	}
    }
    
    public String toJSON(String eqpId) {
    	
    	System.out.printf("[%s] EQPInfo 정보를 JSON 스트링으로 가져옵니다.\n", eqpId);    	
    	StringBuffer sb= new StringBuffer(1000);
    	
    	sb.append("{");
    	
    	if (eqpId.equalsIgnoreCase("ALL")) {
    		sb.append("모든 EQPInfo 정보를 JSON 형태로 변환");
    	} else {
    		sb.append(mapEQPINFO.get(eqpId).toJson());
    	}
    	    	
    	sb.append("}");
    	
    	return sb.toString();
    			
    
    }
}
