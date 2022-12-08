package com.skhynix.neesp;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.skhynix.neesp.util.bwReturnValues;

public class SWNodeInfoManager {

    private static SWNodeInfoManager instance = new SWNodeInfoManager();
    public static SWNodeInfoManager getInstnace() {return instance;}    

    private static Logger logger = Logger.getLogger(SWNodeInfoManager.class.getName());
    
    private Map<String, SWNodeInfo> mapSWNODEINFO = null;
    private String allocationPolicy = "round-robin";
    private String procInstanceId = "";

    
    public SWNodeInfoManager() {
         mapSWNODEINFO = new HashMap<>();
    }
    
    public void setAllocationPolicy(String policy) {this.allocationPolicy = policy;}
    
    public SWNodeInfo getSWNodeInfo(String appNodeName) {
    	if(mapSWNODEINFO.containsKey(appNodeName)) {
            // 이미 있는 경우            
            System.out.printf("/// SW Node가 존재합니다.\n");
            return mapSWNODEINFO.get(appNodeName); 
        } else {
            // 새롭게 만들어진 경우
            System.out.printf("/// SW Node가 존재하지 않습니다.\n");
            return null;
        } 
    }

    public String[] addNewSWNode(String appNodeName, String procInstanceId) {        
        String[] retVals = null;        
        
        this.procInstanceId = procInstanceId;        
        if(mapSWNODEINFO.containsKey(appNodeName)) {
            // 이미 있는 경우 - SWN가 등록되었다가 다시 기동되었다는 의미이다.
            retVals = new bwReturnValues().retVal1("succed-swnode-active", String.format("[%s][%s] 이미 있는 노드입니다.", appNodeName, procInstanceId));
            this.updateSWNodeStatus(appNodeName, "active");
            System.out.printf("/// [addNewSWNode 호출] %s\n", retVals[1]);
            mapSWNODEINFO.get(appNodeName).printSWNodeInfo("updated-swnode-status-when-sw-init");
            
            /*
             *  기존에 등록되어 있는 eqpIds에 있는 장비ID들에 대한 spawn-request를 만들어준다. EDWARD_WON 2022/12/06
             *  Loop를 돌면서 모든 ID를 받아서 요청한다.
             *  
             */
            try {
				Registry.getInstance().spawnSWWorkerRequestQueue.put("EQP1");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        } else {
            // 새롭게 만들어진 경우 
            mapSWNODEINFO.put(appNodeName, new SWNodeInfo(appNodeName,"active"));            
            retVals = new bwReturnValues().retVal1("succed-added-newly", String.format("[%s][%s]이 새롭게 만들어진 노드입니다.", appNodeName, procInstanceId));
            System.out.printf("/// %s\n", retVals[1]);
            mapSWNODEINFO.get(appNodeName).printSWNodeInfo("add-new-and-init-swnode");
        }
        return retVals;
    }
    
    public String[] updateSWNodeStatus(String appNodeName, String status) {
        String[] retVals = null;
        
        if(mapSWNODEINFO.containsKey(appNodeName)) {
            // 이미 있는 경우
            mapSWNODEINFO.get(appNodeName).setSwnodeStatus(status);
            retVals = new bwReturnValues().retVal1("succeed-updated-swnode-status", String.format("/// [%s] 노드의 상태 정보를 수정하였습니다.", appNodeName));
            mapSWNODEINFO.get(appNodeName).printSWNodeInfo("updated-swnode-status");
            
            /*
             * 현재 해당 노드와 관련된 내용은 모두 새롭게 생성을 요청한다.
             * djqtepdlxm 
             */            
            
        } else {
            // 해당하는 노드 정보가 없는 경우
            retVals = new bwReturnValues().retVal1("error-swnode-not-found", String.format("/// [%s] 노드를 찾을 수 없습니다.", appNodeName));
        }      
        return retVals;
    }
    
    
    public String[] releaseSWNode(String appNodeName) {
        String[] retVals = null;
        
        if(mapSWNODEINFO.containsKey(appNodeName)) {
            // 이미 있는 경우
            // mapSWNODEINFO.remove(appNodeName);
        	mapSWNODEINFO.get(appNodeName).setSwnodeStatus("inactive");
            retVals = new bwReturnValues().retVal1("succeed-removed-swnode", String.format("[%s] 해당 노드를 Inactive 상태로 전환합니다.", appNodeName));
            mapSWNODEINFO.get(appNodeName).printSWNodeInfo("release-sw-node-update-status");            
            
        } else {
            // 해당하는 노드 정보가 없는 경우
            retVals = new bwReturnValues().retVal1("error-swnode-not-found", String.format("[%s] 노드를 찾을 수 없습니다.", appNodeName));
            System.out.printf("/// %s\n", retVals[1]);
        }      
        return retVals;
    }
    
    public String[] addNewSWWorker(String eqpId, String appNodeName) {
        String[] retVals = null;
        
        if(mapSWNODEINFO.containsKey(appNodeName)) {
            // 이미 있는 경우
        	if(!mapSWNODEINFO.get(appNodeName).isExitEqpId(eqpId)){
        		mapSWNODEINFO.get(appNodeName).addEqpId(eqpId);
            	retVals = new bwReturnValues().retVal1("succeed-added-eqpid", String.format("[%s] 노드의 [%s] Worker를 추가하였습니다.", appNodeName, eqpId));
            	mapSWNODEINFO.get(appNodeName).printSWNodeInfo("added-new-sw-worker-in-swnode");
        	} else {
        		mapSWNODEINFO.get(appNodeName).printSWNodeInfo("existed-sw-worker-in-swnode");
        	}
        
        } else {
            // 해당하는 노드 정보가 없는 경우
            retVals = new bwReturnValues().retVal1("error-swnode-not-found", String.format("[%s] 노드를 찾을 수 없습니다.", appNodeName));
            System.out.printf("/// %s\n", retVals[1]);
        }      
        return retVals;
    }    
    
    public String[] releaseSWWorker(String eqpId, String appNodeName) {
        String[] retVals = null;
        
        if(mapSWNODEINFO.containsKey(appNodeName)) {
            // 이미 있는 경우 - 삭제가 아닌 inactive 상태로 변경한다.
            mapSWNODEINFO.get(appNodeName).removeEqpId(eqpId);
            retVals = new bwReturnValues().retVal1("succeed-removed-eqpid", String.format("[%s] 노드의 [%s] Worker를 삭제하였습니다.", appNodeName, eqpId));
            mapSWNODEINFO.get(appNodeName).printSWNodeInfo("inactivated-sw-worker-in-swnode");
        } else {
            // 해당하는 노드 정보가 없는 경우
            retVals = new bwReturnValues().retVal1("error-swnode-not-found", String.format("[%s] 노드를 찾을 수 없습니다.", appNodeName));
        }      
        return retVals;
    }
    
    public String[] allocateSWnode(String eqpId, String policy) {    
        String[] retVals = null;        
        String swnodeId = "";
        
        if("round-robin".equalsIgnoreCase(policy)) {
            swnodeId = allocatedByRoundRobin(eqpId);
        } else if("based-resource-usage".equalsIgnoreCase(policy)) {
            swnodeId = allocatedByBasedOnResourceUsage(eqpId);
        } else if("based-eqp-dependency".equalsIgnoreCase(policy)) {
            // 현재 설정되어 있는 정책 기반 - 할당 장비 정보가 있는 경우
            swnodeId = allocatedByBasedOnEqpDependency(eqpId);
        } else {
            // 기본 할당 방법에 따라 설정해준다.
            swnodeId = allocatedByDefaultPolicy(eqpId);
        }
        
        return new bwReturnValues().retVal1("succeed-allocated-swnode", swnodeId);
    }
    
    public String allocatedByRoundRobin(String eqpId) {
        String swnodeId = "sw-node-1";
        System.err.printf("[%s] 장비를 라운드 로빈 정책에 의해서 sw node를 할당합니다.\n", swnodeId);
        return swnodeId;
    }
    
    public String allocatedByBasedOnResourceUsage(String eqpId) {
        String swnodeId = "sw-node-1";
        System.err.printf("[%s] 장비를 리소스 사용량(CPU,Memory,Network IO usage) 기반으로 sw node를 할당합니다.\n", swnodeId);
        return swnodeId;
    }
    
    public String allocatedByBasedOnEqpDependency(String eqpId) {
        String swnodeId = "sw-node-1";
        System.err.printf("[%s] 장비를 메모리 캐쉬 최적화 방식에 의해서 sw node를 할당합니다.\n", swnodeId);
        return swnodeId;
    }
    
    public String allocatedByDefaultPolicy(String eqpId) {
        String swnodeId = "sw-node-1";
        System.err.printf("[%s] 장비를 기본 설정된 방식으로 할당합니다.\n", swnodeId);
        return swnodeId;
    }
}
