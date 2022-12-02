package com.skhynix.neesp;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public String[] addNewSWNode(String appNodeName, String procInstanceId) {        
        String[] retVals = {"",""};        
        
        this.procInstanceId = procInstanceId;
        
        if(mapSWNODEINFO.containsKey(appNodeName)) {
            // 이미 있는 경우
            retVals[0] = "updated";
            retVals[1] = String.format("[%s][%s] 이미 있는 노드입니다.", appNodeName, procInstanceId);
        } else {
            // 새롭게 만들어진 경우
            mapSWNODEINFO.put(appNodeName, new SWNodeInfo(appNodeName,"ready"));
            retVals[0] = "added-newly";
            retVals[1] = String.format("[%s][%s]이 이미 있는 노드입니다.", appNodeName, procInstanceId);            
        }      
        return retVals;
    }
    
    public String[] updateSWNodeStatus(String appNodeName, String status) {
        String[] retVals = {"",""};
        
        if(mapSWNODEINFO.containsKey(appNodeName)) {
            // 이미 있는 경우
            mapSWNODEINFO.get(appNodeName).setSwnodeStatus(status);
            retVals[0] = "updated-swnode-status";
            retVals[1] = String.format("[%s] 노드의 상태 정보를 수정하였습니다.", appNodeName);
        } else {
            // 해당하는 노드 정보가 없는 경우            
            retVals[0] = "error-swnode-not-found";
            retVals[1] = String.format("[%s] 노드를 찾을 수 없습니다.", appNodeName);
        }      
        return retVals;
    }
    
    
    public String[] releaseSWNode(String appNodeName) {
        String[] retVals = {"",""};
        
        if(mapSWNODEINFO.containsKey(appNodeName)) {
            // 이미 있는 경우
            mapSWNODEINFO.remove(appNodeName);
            retVals[0] = "removed-swnode";
            retVals[1] = String.format("[%s] 노드를 삭제하였습니다.", appNodeName);
        } else {
            // 해당하는 노드 정보가 없는 경우            
            retVals[0] = "error-swnode-not-found";
            retVals[1] = String.format("[%s] 노드를 찾을 수 없습니다.", appNodeName);
        }      
        return retVals;
    }
    
    public String[] addNewSWWorker(String eqpId, String appNodeName) {
        String[] retVals = {"",""};
        
        if(mapSWNODEINFO.containsKey(appNodeName)) {
            // 이미 있는 경우
            mapSWNODEINFO.get(appNodeName).addEqpId(eqpId);
            retVals[0] = "added-eqpid";
            retVals[1] = String.format("[%s] 노드의 [%s] Worker를 추가하였습니다.", appNodeName, eqpId);
        } else {
            // 해당하는 노드 정보가 없는 경우            
            retVals[0] = "error-swnode-not-found";
            retVals[1] = String.format("[%s] 노드를 찾을 수 없습니다.", appNodeName);
        }      
        return retVals;
    }    
    
    public String[] releaseSWWorker(String eqpId, String appNodeName) {
        String[] retVals = {"",""};
        
        if(mapSWNODEINFO.containsKey(appNodeName)) {
            // 이미 있는 경우
            mapSWNODEINFO.get(appNodeName).removeEqpId(eqpId);
            retVals[0] = "removed-eqpid";
            retVals[1] = String.format("[%s] 노드의 [%s] Worker를 삭제하였습니다.", appNodeName, eqpId);
        } else {
            // 해당하는 노드 정보가 없는 경우            
            retVals[0] = "error-swnode-not-found";
            retVals[1] = String.format("[%s] 노드를 찾을 수 없습니다.", appNodeName);
        }      
        return retVals;
    }
    
    public String[] allocateSWnode(String eqpId, String policy) {    
        String[] retVals = {"",""};        
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
        
        retVals[0] = "ok-allocated";
        retVals[1] = swnodeId;
        
        return retVals;
    }
    
    public String allocatedByRoundRobin(String eqpId) {
        String swnodeId = "sw-node-1";
        return swnodeId;
    }
    
    public String allocatedByBasedOnResourceUsage(String eqpId) {
        String swnodeId = "sw-node-1";
        return swnodeId;
    }
    
    public String allocatedByBasedOnEqpDependency(String eqpId) {
        String swnodeId = "sw-node-1";
        return swnodeId;
    }
    
    public String allocatedByDefaultPolicy(String eqpId) {
        String swnodeId = "sw-node-1";
        return swnodeId;
    }
}
