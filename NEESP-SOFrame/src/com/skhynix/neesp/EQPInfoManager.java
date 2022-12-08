package com.skhynix.neesp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.skhynix.neesp.util.bwReturnValues;

public class EQPInfoManager {
	
    private static Logger logger = Logger.getLogger(EQPInfoManager.class.getName());
    
    private static EQPInfoManager instance = new EQPInfoManager();
    public static EQPInfoManager getInstance() {return instance;}
    
    private Map<String, EQPInfo> mapEQPINFO = null;
    
    public EQPInfoManager() {
        mapEQPINFO = new HashMap<>();
        System.out.printf("/// EQPInfoManager 생성자\n");
    }
    
    public EQPInfo getEQPInfo(String eqpId) {
        if(mapEQPINFO.containsKey(eqpId)) {
            // 상태 정보에 따라서 처리 방식 결정
            return mapEQPINFO.get(eqpId);
        }
        return null;
    }
    
    public EQPInfo getEQPInfo(String eqpId, String reqCommand) {
        if(mapEQPINFO.containsKey(eqpId)) {
            System.out.printf("[%s] 해당 장비 관련 정보를 요청 명렁어가 있습니다. [%s]", eqpId, reqCommand); 
            // 상태 정보에 따라서 처리 방식 결정
            return mapEQPINFO.get(eqpId);
        }
        return null;
    }
    
    public String checkEqpsStatus() {
    	ArrayList<String> eqpStatus = new ArrayList<>();
    	
    	if(mapEQPINFO.size() > 0 ) {
	    	Set<String> keySet = mapEQPINFO.keySet();
	    	for(String eqpId : keySet) {
	    		long intervalAfterLastCheck = System.currentTimeMillis()-mapEQPINFO.get(eqpId).getLastUpdateTime();
	    		if(intervalAfterLastCheck > 30000) {
	    			// mapEQPINFO.get(eqpId).setWorkerStatus("deactivated");
	    			System.err.println(mapEQPINFO.get(eqpId).getEQPInfo("update-worker-status-deactivated"));
	    			eqpStatus.add(eqpId);
	    		}
	    	}
	    	
	    	return eqpStatus.toString();
    	} else {
    		return "no-eqps";
    	}
    } 
    
    public String[] initEQPInfo(String eqpId, String inboundQueue) {
    	
        if(mapEQPINFO.containsKey(eqpId)) {
            System.out.printf("[%s][%s] 이미 존재하는 장비 입니다.\n", eqpId, inboundQueue); 
            // 상태 정보에 따라서 처리 방식 결정
            return new bwReturnValues().retVal1("error-already-exist", String.format("[%s][%s] 해당하는 장비는 이미 존재합니다. 초기화 할 수 없습니다.", eqpId, inboundQueue));
        } else {
            mapEQPINFO.put(eqpId, new EQPInfo(eqpId, inboundQueue, "ready"));            
            return new bwReturnValues().retVal1("succeed-init-eqpinfo", String.format("[%s][%s] 장비를 정상적으로 초기화 하였습니다.", eqpId, inboundQueue));
        }
    }   
    
    public String[] setSWNodeId(String eqpId, String allocatedSWNodeId, String eqpStatus) {
                
        if(mapEQPINFO.containsKey(eqpId)) {
            // 상태 정보에 따라서 처리 방식 결정
            mapEQPINFO.get(eqpId).setSwnodeId(allocatedSWNodeId);
            mapEQPINFO.get(eqpId).setEqpStatus(eqpStatus);
            String logMsg = String.format("[%s]가 [%s] 노드에 할당되었습니다. [%s]", eqpId, allocatedSWNodeId, eqpStatus);
            logger.log(Level.INFO, logMsg);                        
            return new bwReturnValues().retVal1("succeed-set-swnodeid", String.format("[%s][%s] 장비를 위한 SW Node를 설정하였습니다.",eqpId, allocatedSWNodeId));
            
        } else {
            // 오류에 따른 처리 진행
            System.out.printf("[%s] 존재하지 않는 장비 입니다.\n", eqpId);
            return new bwReturnValues().retVal1("error-eqp-not-found", String.format("[%s][%s] 해당하는 장비를 찾을 수 없습니다.",eqpId, eqpStatus));
        }     
    }
    
    public String[] changeEQPInfo(String eqpId, String swnodeId, String workerId, String eqpStatus, String workerStatus, String jobTitle) {
        String[] retVals = {"",""};        
        if(mapEQPINFO.containsKey(eqpId)) {
            // 상태 정보에 따라서 처리 방식 결정
            mapEQPINFO.get(eqpId).setSwnodeId(swnodeId);
            mapEQPINFO.get(eqpId).setEqpStatus(eqpStatus);
            mapEQPINFO.get(eqpId).setWorkerId(workerId);
            mapEQPINFO.get(eqpId).setWorkerStatus(workerStatus);            
            logger.log(Level.INFO, retVals[1]);
            System.out.printf("/// 상태 정보를 변경하였습니다 : %s\n",retVals[1]);                        
            return new bwReturnValues().retVal1("succeed-changed-eqp-info", String.format("[%s][%s] 장비 정보를 변경하였습니다. [작업명: %s] ",eqpId, eqpStatus, jobTitle));
        } else {
            // 오류에 따른 처리 진행
            System.out.println(retVals[1]); 
            return new bwReturnValues().retVal1("error-eqp-not-found", String.format("[%s] 해당 장비를 찾을 수 없습니다. [작업명: %s]",eqpId, jobTitle));
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
            System.out.printf("[%s] 존재하지 않는 장비 입니다. [작업제목:%s]\n", eqpId, jobTitle); 
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
    		// 나중에 필요할 때 만들어서 사용한다.
    		sb.append("모든 EQPInfo 정보를 JSON 형태로 변환");
    	} else {
    		sb.append(mapEQPINFO.get(eqpId).toJson());
    	}
    	    	
    	sb.append("}");
    	
    	return sb.toString();
    			
    
    }
}
