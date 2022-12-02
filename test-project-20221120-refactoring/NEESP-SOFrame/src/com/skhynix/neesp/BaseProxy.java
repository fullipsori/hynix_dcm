package com.skhynix.neesp;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hynix.common.StringUtil;
import com.skhynix.controller.BusinessLogic;
import com.skhynix.controller.MessageRouter;
import com.skhynix.manager.BusinessManager;
import com.skhynix.manager.DynaClassManager;
import com.skhynix.manager.MessageManager;
import com.skhynix.model.EmsSessModel;
import com.skhynix.neesp.log.LogManager;
import com.skhynix.neesp.log.NEESPLogger;
import com.skhynix.neesp.util.Counter;
import com.skhynix.neesp.util.RandomCollection;

import io.reactivex.rxjava3.core.Single;


public class BaseProxy {
	
	private static Logger logger = Logger.getLogger(BaseProxy.class.getName());
	private static RandomCollection<String> rc = new RandomCollection<String>().add(70, "TOOLDATA").add(15, "EVENT").add(10, "RECIPE").add(10, "ALARM");
	private static Counter testCounter = new Counter("testCounter");
	private NEESPLogger eqpLogger = null;
	
	/** setupObservable 이 필요한 항목들은 모두 초기화 해두자 **/ 
	private final DynaClassManager dynaClassManager = DynaClassManager.getInstance();
	private final MessageRouter messageRouter = MessageRouter.getInstance();
	private final BusinessManager businessManager = BusinessManager.getInstance();
	private final BusinessLogic businessLogic = BusinessLogic.getInstance();

	public BaseProxy() {
		// 생성자
		System.out.println("/// BaseProxy called");
		weightBasedRandomValue();
		
		String parameter = "EQP1";
		System.out.println("/// 최초 생성- 장비 생성시점에 같이 생성한다. NEESPLogger: " + parameter);		
		eqpLogger = new NEESPLogger(parameter, "sw-node-1", LogManager.getInstance().getLogger());		
		LogManager.getInstance().startMonitor();
	}
	
	@SuppressWarnings("unchecked")
	public String openSession(String domain, String jsonString) {
		if(StringUtil.isEmpty(domain) || StringUtil.isEmpty(jsonString)) return "";
		Map<String, Object> params = StringUtil.jsonToObject(jsonString, Map.class);
		return messageRouter.openSession(domain, (String)params.get("serverUrl"), jsonString);
	}
	
	public void closeSession(String handle) {
		messageRouter.closeSession(handle);
	}
	
	public void sendMessage(String handle, String data) {
		/**
		Observable.intervalRange(1, 20, 100L, 100L, TimeUnit.MILLISECONDS)
			.subscribe(Void -> {
				messageRouter.sendMessage(handle, data);
			});
			**/
		messageRouter.sendMessage(handle, data);
	}
	
	public String receiveMessage(String handle) {
		/**
		Observable.intervalRange(1, 20, 100L, 100L, TimeUnit.MILLISECONDS)
			.subscribe(Void -> {
				System.out.println("result:" + messageRouter.receiveMessage(handle));
			});
			**/
		return messageRouter.receiveMessage(handle);
	}
	
	public String doBusiness(String eventType, String message) {
		try {
			return businessLogic.doBusiness(eventType, message, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}
	
	public boolean loadJar(String className, String classPath) {
		return dynaClassManager.loadJar(className, classPath);
	}
	
	public boolean unloadJar(String className) {
		return dynaClassManager.unloadJar(className);
	}
	
	
	public String[] moduleTest(String command, String parameter, String parameter2, String parameter3, String parameter4) {
		String[] retVals = {"",""};
		
		System.out.println("1.moduleTest");
		eqpLogger.syslog(parameter4);
		System.out.println("2.moduleTest - sysLogOff");
		eqpLogger.syslogOff();
		eqpLogger.syslog("sys log 외부로 내보는 것을 종료하였습니다. - KAFKA로 출력이 되나요.");
		System.out.println("3.moduleTest - rtOnOff ["+eqpLogger.isSyncLog()+"]을 종료하고 Queue를 이용한 비동기 방식으로 메시지를 전송합니다.");
		eqpLogger.syslogOn();
		eqpLogger.realtimeLogOff();
		for(int i=0; i < 100; i++) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			eqpLogger.syslog("메시지를 보내는데 큐를 이용해서 버퍼링 후 전송합니다.. - KAFKA로 출력이 되나요. ("+(i+1)+")");
		}
				
		retVals[0] = "로그매니저 테스트";
		retVals[1] = "로그매니저 테스트가 정상적으로 수행되었습니다.";
		
		return retVals;
	}
	
	/*
	 * Simulation & Utility 함수 테스트 
	 * 1) Weighted Random
	 * 2) Counter 
	 */
	public void weightBasedRandomValue() {
		// 랜덤 함수로 이벤트를 생성하기 위하여 테스트 한다.
		
		for (int i = 0; i < 10; i++) {
			String randomVal = rc.next();
			testCounter.increaseCount(randomVal);
		} 
		
		String jsonVal = testCounter.printAllCounterAll();
		System.out.println(jsonVal);
	}
	
	public String[] GenerateEventMsg(String eqpId, String message, long msgId) {
		
		String[] retVals = {"","","",""};		
		StringBuffer sb = new StringBuffer(100);
		
		sb.append("{");
		sb.append(String.format("\"eqpId\":\"%s\",",eqpId));
		sb.append(String.format("\"eventType\":\"%s\",",rc.next()));
		sb.append(String.format("\"msgId\":\"%s\",",String.format("%s-%d", eqpId, msgId)));
		sb.append(String.format("\"messageBody\":\"%s\",",message));
		sb.append(String.format("\"reserved\":\"%s\"","reserved"));
		sb.append("}");
		
		retVals[0] = eqpId;
		retVals[1] = sb.toString();
		
		System.out.printf("[%s]\n", sb.toString());
		
		return retVals;
	}
	
	
	/*
	 * Transaction Log 관련 체크 함수 모듬
	 */
	
	public String[] trxStartLog(String eqpId, String eventType, String msgId, String message, String log) {
		String[] retVals = {"",""};
		String gxtnId = "gtxnId";
		String extnId = "extnId";		
		SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getLogger().logTrxStart(log, eventType, gxtnId, extnId, message);		
		return retVals;
	}
	
	public String[] trxProcLog(String eqpId, String log) {
		String[] retVals = {"",""};
		SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getLogger().logTrxProc(log);		
		return retVals;
	}
	
	public String[] trxEndLog(String eqpId, String log) {
		String[] retVals = {"",""};
		SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getLogger().logTrxEnd(eqpId);
		return retVals;
	}
	
	
	/*
	 * NEESP-SO 수행에 필요한 함수
	 */
    public String[] addNewSWNode(String appNodeName, String procInstanceId) {
        String[] retVals = {"",""};        
        String logMsg = String.format("[%s][%s] 새로운 SWNode Id를 추가하였습니다.", appNodeName, procInstanceId);
        retVals = SWNodeInfoManager.getInstnace().addNewSWNode(appNodeName, procInstanceId);
        return retVals;      
    }
    
    public String[] releaseSWNode(String appNodeName) {
        String[] retVals = {"",""};
        String logMsg = String.format("[%s] SWNode Id를 제거하였습니다. [SO]", appNodeName);
        retVals = SWNodeInfoManager.getInstnace().releaseSWNode(appNodeName);
        return retVals;      
    }    
    
    public String[] initSWWorker(String eqpId) {
        String[] retVals = {"",""};        
        String logMsg = String.format("[%s] 새로운 SW Worker를 초기화하였습니다. [SO]", eqpId);
        retVals = EQPInfoManager.getInstance().initEQPInfo(eqpId);        
        EQPInfoManager.getInstance().printEQPInfo(eqpId,"init-new-worker");
        
        return retVals;
    }
    
    public String[] addNewSWWorker(String eqpId, String appNodeName, String procInstanceId) {
        String[] retVals = {"",""};        
        String logMsg = String.format("[%s][%s][%s] 새로운 SW Worker를 추가하였습니다. [SO]", eqpId, appNodeName, procInstanceId);
        retVals = SWNodeInfoManager.getInstnace().addNewSWWorker(eqpId, appNodeName);
        retVals = EQPInfoManager.getInstance().changeEQPInfoWhenCreated(eqpId, appNodeName, procInstanceId, "active","ready");
        EQPInfoManager.getInstance().printEQPInfo(eqpId,"add-new-worker");
        return retVals;
    }
    
    
    public String[] releaseSWWorker(String eqpId, String appNodeName) {
        String[] retVals = {"",""};        
        String logMsg = String.format("[%s][%s] 새로운 SW Worker를 노드에서 제거하였습니다. [SO]", eqpId, appNodeName);
        retVals = SWNodeInfoManager.getInstnace().releaseSWWorker(eqpId, appNodeName);
        retVals = EQPInfoManager.getInstance().changeEQPInfoWhenReleased(eqpId, "", "", "deactive","stopped");
        EQPInfoManager.getInstance().printEQPInfo(eqpId,"add-new-worker");
        return retVals;
    }

	public String[] allocatSWN_forNEWEQP(String eqpId) {
		String[] retVals = {"",""};
		// String logMsg = String.format("{\"eqp_id\":\"%s\",\"swnode_id\":\"%s\",\"msg\":\"%s\"}", eqpId, "swnode_01","새롭게 장비가 추가되었습니다. Worker 생성 필요.")		
        String swnodeId = SWNodeInfoManager.getInstnace().allocateSWnode(eqpId, "round-robin")[1];	
		EQPInfoManager.getInstance().setSWNodeId(eqpId, swnodeId, "wait-worker-ready");
		EQPInfoManager.getInstance().printEQPInfo(eqpId, "allocated-swnode");
		
        StringBuffer sb = new StringBuffer(100);
		
		sb.append("{");
		sb.append(String.format("\"command\":\"%s\",","spawn-request"));
		sb.append(String.format("\"parameter\":\"%s\",",eqpId));
		sb.append(String.format("\"parameter2\":\"%s\",",swnodeId));
		sb.append(String.format("\"parameter3\":\"%s\",","round-robin 할당 방식"));
		sb.append(String.format("\"parameter4\":\"%s\"",String.format("새로운 장비 [%s]가 추가되었습니다.", eqpId)));
		sb.append("}");
		
		retVals[0] = String.format("queue.so.%s.ctl", swnodeId); // 장비 제어용 토픽 토픽 진행
    	retVals[1] = sb.toString();		

		logger.log(Level.INFO,String.format("[%s] 장비 할당 요청 이벤트 메시지 발신 [%s]", eqpId, retVals[1]));
		
		return retVals;
	}
	
	/*
	 * stop-request
	 */
	public String[] stopSWWorker(String eqpId, String reqCommand) {
		
		String[] retVals = {"",""};		
		EQPInfo eqpInfo = null;
		
		if((eqpInfo =EQPInfoManager.getInstance().getEQPInfo(eqpId, reqCommand)) == null) {
			
			return retVals;
		}
		
		String swnodeId = eqpInfo.getSwnodeId();
		String procInstanceId = eqpInfo.getWorkerId();
		
		StringBuffer sb = new StringBuffer(100);		
		sb.append("{");
		sb.append(String.format("\"command\":\"%s\",","stop-request"));
		sb.append(String.format("\"parameter\":\"%s\",",eqpId));
		sb.append(String.format("\"parameter2\":\"%s\",",swnodeId));
		sb.append(String.format("\"parameter3\":\"%s\",",procInstanceId));
		sb.append(String.format("\"parameter4\":\"%s\"","SWN 관련 정리 작업을 부탁드립니다."));
		sb.append("}");
		
		retVals[0] = String.format("queue.so.%s.ctl", swnodeId);
		retVals[1] = sb.toString();
		
		return retVals;
	}
	
    /*
     * NEESP-SWN 수행에 필요한 함수 목록
     */
    public String[] checkSWorkerInfo(String command, String parameter, String parameter2, String parameter3, String parameter4) {
        String[] retVals = {"","","",""}; 
        String eqpId = parameter;
        String swnodeId = parameter2;
        
        retVals[0] = command; // spawn-request
        retVals[1] = eqpId;
        retVals[2] = swnodeId;
        retVals[3] = String.format("[%s] 장비를 위한 Worker 생성 요청", eqpId);

        if("spawn-request".equalsIgnoreCase(command)) {        	
            boolean bExist = SWWorkerInfoManager.getInstance().initSWWorkerInfo(eqpId, swnodeId);            
            if(bExist) {
            
                SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setSwnodeId(swnodeId);
                SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setWorkerStatus("ready");
                if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getWorkerStatus().equalsIgnoreCase("stop") == false) {
                    retVals[0] = "use-exist-worker-thread";
                } else {
                    System.out.printf("[%s] Thread Spawning 필요\n", eqpId);
                }
                
                SWWorkerInfoManager.getInstance().printSWWorkerInfo(eqpId,"check-swworker-info");
            }            
          
        } else if("stop-request".equalsIgnoreCase(command)) {
            if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId) != null) {
                SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setSORequest("stop-request");
                System.out.printf("[%s] 작업자 정지 요청\n", eqpId);
                SWWorkerInfoManager.getInstance().printSWWorkerInfo(eqpId,"set-stop-request");
            } else {
                System.out.printf("[%s] 장비를 위한 작업자가 없습니다.\n", eqpId);  
            }
        }
        
                      
        return retVals;
    }
    
    public String[] prepareAndCheckDup(String eqpId, String messageId) {
        String[] retVals = {"",""}; 
        
        retVals[0] = "succeed-prepare-to-data";
        retVals[1] = String.format("[%s][%s] 수신 메시지가 중복 처리 유무를 확인합니다.", eqpId, messageId);

        if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId)==null) {
             retVals[0] = "error-swworker-not-found";
             retVals[1] ="찾을 수 없습니다.";
             return retVals;
        }
        
        /*
         * 1. eqpId, gtxId.msg 파일 유무를 체크
         *        - 없는 경우: 신규 메시지라른 의미
         *        - 있는 경우: 처리 중 종료된 메시지가 존재           
         * 2. 있는 경우 - 진행 상태를 체크
         * 		  - ready
         *        - processed
         *        - sent
         *        - confirmed
         * 3. 처리진행 상태 값에 따라 해당 작업으로 스킵한다.
         *                           
         */

        SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setMessageId(messageId);
        SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setMsgProcStage("ready");
        SWWorkerInfoManager.getInstance().printSWWorkerInfo(eqpId,"prepare-to-check-dup");
        
        retVals[0] = SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getMsgProcStage();
        
        return retVals;
    }
    
     public String[] doBusiness(String eqpId, String appNodeName, String eventType, String messge) {
        String[] retVals = {"",""}; 
        
        System.out.println("doBusiness");
        retVals[0] = "succeed-do-business";
        retVals[1] = String.format("[%s][%s] 메시지 작업을 수행합니다.", eqpId, eventType);

        if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId)==null) {
             retVals[0] = "error-swworker-not-found-do-business";
             retVals[1] ="찾을 수 없습니다.";
             return retVals;
        }
        
        /*
         * 1. 처리 상태 마킹
         * 2. 이벤트 타입에 따라 Dynamic Business Logic Library에서 처리 대상을 가져온다.
         * 3. eqpId.messageId.msg 파일값을 overwrite 해준다.
         */

        SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setMsgProcStage("process-start");
        SWWorkerInfoManager.getInstance().printSWWorkerInfo(eqpId,"begin-do-business");
        
        // DynamicLoading.getSWWorkerInfo(eqpId).doBusiness(eventType, message);
        SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setMsgProcStage("process-end");
        SWWorkerInfoManager.getInstance().printSWWorkerInfo(eqpId,"end-do-business");  
        
        // 처리 이벤트 카운트 하기
        SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).increaseEventCount(eventType);
        
        // fullipsori
        doBusiness(eventType, messge);
        return retVals;
    }
     
    public String[] checkNextStepWhenGetTimeout(String eqpId, String appNodeName, String procInstanceId) {
    	String[] retVals = {"",""};
    	
		/*
		 * 1. SWWorkerInfo 상태 체크: 
		 * 2. 요청 메시지 항목이 무엇인지 확인하고 작업을 수행한다.
		 * 3. 작업이 완료되었다면 결과 메시지를 만든다.
		 */
        if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId) == null) {           
           retVals[0] = "error-not-found-swworker";
           retVals[1] = String.format("[%s] 장비의 SWWorkerInfo 정보가 없습니다. 동기화를 위한 메시지가 필요합니다.", eqpId);           
        } else if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getSORequest().equalsIgnoreCase("stop-request")) {
        
            StringBuffer sb = new StringBuffer(100);		
	    	sb.append("{");
		    sb.append(String.format("\"command\":\"%s\",","release-swworker"));
    		sb.append(String.format("\"parameter\":\"%s\",",eqpId));
    		sb.append(String.format("\"parameter2\":\"%s\",",appNodeName));
	    	sb.append(String.format("\"parameter3\":\"%s\",",procInstanceId));
		    sb.append(String.format("\"parameter4\":\"%s\"", "SWNode Worker가 삭제되었습니다. SO내 EQPInfo 정보의 변경이 필요합니다."));
    		sb.append("}");
    		
    		retVals[0] = "succeed-release-swworker";
    		retVals[1] = sb.toString();
        
        } else {
        
           retVals[0] = "error-no-match-command";
        }
		
		logger.log(Level.INFO,String.format("[%s][%s] checkMsgFromSO 후 작업을 마무리 하겠습니다.", eqpId, procInstanceId));
		
		return retVals;
 	}
    
    public String[] makeTimeoutEventMsg(String eqpId, String appNodeName, String procInstanceId) {
		String[] retVals = {"",""};
		StringBuffer sb = new StringBuffer(100);
		
		sb.append("{");
		sb.append(String.format("\"command\":\"%s\",","getmsg-timeout"));
		sb.append(String.format("\"parameter\":\"%s\",",eqpId));
		sb.append(String.format("\"parameter2\":\"%s\",",appNodeName));
		sb.append(String.format("\"parameter3\":\"%s\",",procInstanceId));
		sb.append(String.format("\"parameter4\":\"%s\"","Get Message Timeout 이벤트가 발생하였습니다."));
		sb.append("}");
		
		retVals[0] = "succeed-handle-timeout-event";
		retVals[1] = sb.toString();
		
		return retVals;
	}
		
	public String initializeSWN(String appNodeName, String applicationName, String procInstanceId ) {
		String[] retVals = {"",""};	
		String retVal = String.format("[%s][%s][%s] 초기화 작업을 수행해야 합니다.", appNodeName, applicationName, procInstanceId);
		
//		LogManager.getInstance().initialize(appNodeName);
//		logger.addHandler(LogManager.getInstance().getKafkaHandler(appNodeName,applicationName, ""));
//		logger.log(Level.INFO, retVal);
//		logger.log(Level.INFO, "기본 설정 작업 마무리 후 메시지 전달하기 ");
		
		StringBuffer sb = new StringBuffer(100);
		
		// {"command":"load-jar", "parameter":"AStest","parameter2":"D:/00_SKhynix/test-project/test-as", "parameter3":"doBusiness","parameter4":""}
		
		sb.append("{");
		sb.append(String.format("\"command\":\"%s\",","init-swnode"));
		sb.append(String.format("\"parameter\":\"%s\",",appNodeName));
		sb.append(String.format("\"parameter2\":\"%s\",",applicationName));
		sb.append(String.format("\"parameter3\":\"%s\",",procInstanceId));
		sb.append(String.format("\"parameter4\":\"%s\"","정상적으로 초기화가 되었습니다."));
		sb.append("}");
		
		return sb.toString();
	}
	
	public String releaseSWN(String appNodeName, String applicationName, String procInstanceId) {
		String[] retVals = {"",""};	
		String retVal = String.format("[%s][%s][%s] 모든 정리작업을 수행하였습니다. - 마무리 처리 부탁합니다.", appNodeName, applicationName, procInstanceId);
		LogManager.getInstance().initialize(appNodeName);
		logger.addHandler(LogManager.getInstance().getKafkaHandler(appNodeName,applicationName, ""));
		logger.log(Level.INFO, retVal);
		logger.log(Level.INFO, "기본 설정 작업 마무리 후 메시지 전달하기 ");
		
		StringBuffer sb = new StringBuffer(100);		
		sb.append("{");
		sb.append(String.format("\"command\":\"%s\",","release-swnode"));
		sb.append(String.format("\"parameter\":\"%s\",",appNodeName));
		sb.append(String.format("\"parameter2\":\"%s\",",applicationName));
		sb.append(String.format("\"parameter3\":\"%s\",",procInstanceId));
		sb.append(String.format("\"parameter4\":\"%s\"","SWN 관련 정리 작업을 부탁드립니다."));
		sb.append("}");
		
		return sb.toString();
	}
	
	public String[] initSWWorker(String eqpId, String appNodeName, String applicationName, String procInstanceId) {
		
		/*
		 * 1. 초기화 작업을 진행합니다.
		 */		
		String[] retVals = {"",""};		 
		retVals[1] = String.format("%s|%s|%s|%s| 장비 큐 처리용 작업자 정상적으로 생성되었습니다..", eqpId, appNodeName, applicationName, procInstanceId);
		
		if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId) != null) {
            SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setSwnodeId(appNodeName);
            SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setWorkerId(procInstanceId);
            SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setWorkerStatus("ready");
            SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setMessageId("n/a");
            SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setMsgProcStage("n/a");
            SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getSWWorkerInfo("init-swworker");
            
            StringBuffer sb = new StringBuffer(100);		
    		sb.append("{");
    		sb.append(String.format("\"command\":\"%s\",","init-swworker"));
    		sb.append(String.format("\"parameter\":\"%s\",",eqpId));
    		sb.append(String.format("\"parameter2\":\"%s\",",appNodeName));
    		sb.append(String.format("\"parameter3\":\"%s\",",procInstanceId));
    		sb.append(String.format("\"parameter4\":\"%s\"", String.format("[%s] 정상적으로 초기화 되었습니다.", eqpId)));
    		sb.append("}");
    		
            retVals[0] = "succeed-init-swworker";
            retVals[1] = sb.toString(); 

         } else {
            // 없는 경우
           retVals[0] = "error-not-found-swworker";           
           StringBuffer sb = new StringBuffer(100);		
   		   sb.append("{");
   		   sb.append(String.format("\"command\":\"%s\",","error-init-swworker"));
   		   sb.append(String.format("\"parameter\":\"%s\",",eqpId));
   		   sb.append(String.format("\"parameter2\":\"%s\",",appNodeName));
   		   sb.append(String.format("\"parameter3\":\"%s\",",procInstanceId));
   		   sb.append(String.format("\"parameter4\":\"%s\"", String.format("[%s] SWWorker 생성에 실패하였습니다. [error-code:%s]", eqpId, retVals[0])));
   		   sb.append("}");
   		   retVals[1] = sb.toString();
         }
		
		logger.log(Level.INFO,String.format("[%s][%s] 장비를 위한 SW-Worker 초기화 작업을 완료하였습니다.", eqpId, procInstanceId));
		
		return retVals;
	}
	
	public String releaseSWWorker(String eqpId, String appNodeName, String applicationName, String procInstanceId) {
    	String[] retVals = {"",""};
		String retVal = String.format("%s|%s|%s|%s| 장비 큐 처리용 작업자가 정리되었습니다.", eqpId, appNodeName, applicationName, procInstanceId);
		
        if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId) != null) {
           SWWorkerInfoManager.getInstance().releaseSWWorkerInfo(eqpId);          
           retVals[0] = "succeed-removed-swworker";
        } else {
           retVals[0] = "error-not-found-swworker";
        }
		
		StringBuffer sb = new StringBuffer(100);
		sb.append("{");
		sb.append(String.format("\"command\":\"%s\",","release-swworker"));
		sb.append(String.format("\"parameter\":\"%s\",",eqpId));
		sb.append(String.format("\"parameter2\":\"%s\",",appNodeName));
		sb.append(String.format("\"parameter3\":\"%s\",",procInstanceId));
		sb.append(String.format("\"parameter4\":\"%s\"",retVal));
		sb.append("}");
		
		return sb.toString();
	}
	
	public String updateSWWorkerInfo(String messageBody) {
		logger.log(Level.INFO, String.format("[%s]",messageBody));
		return String.format("[%s]",messageBody);
	}
	
	public String[] readyToNext(String eqpId, String appNodeName, String applicationName, String procInstanceId) {
    	String[] retVals = {"",""};
		
        if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId) != null) {
        	
        	if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getSORequest().equalsIgnoreCase("stop-request")) {        		
        		StringBuffer sb = new StringBuffer(100);
        		sb.append("{");
        		sb.append(String.format("\"command\":\"%s\",","release-swworker"));
        		sb.append(String.format("\"parameter\":\"%s\",",eqpId));
        		sb.append(String.format("\"parameter2\":\"%s\",",appNodeName));
        		sb.append(String.format("\"parameter3\":\"%s\",",procInstanceId));
        		sb.append(String.format("\"parameter4\":\"%s\"", "SWnode Worker를 제거하였습니다."));
        		sb.append("}");
        		
        		retVals[0] = "stop-request";
        		retVals[1] = sb.toString();
        		
        	} else {	           
        		retVals[0] = "succeed-ready-to-next";
        		retVals[1] = String.format("[%s][%s] 메시지 처리가 잘 수행하고 다음 작업을 수행합니다.", eqpId, SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getMessageId());
        		
        		SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setWorkerStatus("ready");
        		SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setMsgProcStage("ready");
        		SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setMessageId("wait-new-event-msg");
        	}
        } else {
           retVals[0] = "error-not-found-swworker";
           retVals[0] =  String.format("[%s] 장비를 찾을 수 없습니다. 확인이 필요합니다.", eqpId);
        }
		
		logger.log(Level.INFO,String.format("[%s][%s] 다음 처리를 위하여 준비합니다. ReadyToNext: [다음 작업: %s]", eqpId, procInstanceId, retVals[1]));		
		SWWorkerInfoManager.getInstance().printSWWorkerInfo(eqpId, "ready-to-next");
		SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).printCounterInfo();
		
		return retVals;
	}
	
	public String[] checkMsgFromSO(String eqpId, String appNodeName, String applicationName, String procInstanceId) {		
    	String[] retVals = {"",""};
    	
		/*
		 * 1. SWWorkerInfo 상태 체크: 
		 * 2. 요청 메시지 항목이 무엇인지 확인하고 작업을 수행한다.
		 * 3. 작업이 완료되었다면 결과 메시지를 만든다.
		 */
        if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId) == null) {
           
           retVals[0] = "error-not-found-swworker";
           retVals[1] = String.format("[%s] 장비의 SWWorkerInfo 정보가 없습니다. 체크 알람 메시지 생성 필요", eqpId);
           
        } else if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getSORequest().equalsIgnoreCase("stop-request")) {
        
        
            StringBuffer sb = new StringBuffer(100);		
	    	sb.append("{");
		    sb.append(String.format("\"command\":\"%s\",","release-swworker"));
    		sb.append(String.format("\"parameter\":\"%s\",",appNodeName));
    		sb.append(String.format("\"parameter2\":\"%s\",",applicationName));
	    	sb.append(String.format("\"parameter3\":\"%s\",",procInstanceId));
		    sb.append(String.format("\"parameter4\":\"%s\"", eqpId));
    		sb.append("}");
    		
    		retVals[0] = "succeed-release-worker";
    		retVals[1] = sb.toString();
        
        } else {
        
           retVals[0] = "error-no-match-command";
        }
		
		logger.log(Level.INFO,String.format("[%s][%s] checkMsgFromSO 후 작업을 마무리 하겠습니다.", eqpId, procInstanceId));
		
		return retVals;
	}
	
	
	
	/*
 	* Activate Startup 관련 메시지
 	*/
	public String onStartUpEvent(String appNodeName, String applicationName, String procInstanceId ) {
		String retVal = String.format("[%s][%s][%s] 초기화 작업을 수행해야 합니다.", appNodeName, applicationName, procInstanceId);
		LogManager.getInstance().initialize(appNodeName);		
//		logger.addHandler(LogManager.getInstance().getKafkaHandler(appNodeName,applicationName, ""));
		logger.log(Level.INFO, "test logger in BaseProxy");
		logger.log(Level.INFO, retVal);
		return retVal;
	}
	
	public String onShutdownEvent(String appName, String engineName, String procInstanceId ) {
		String retVal = String.format("[%s][%s][%s] 종료전 정리 작업을 수행해야 합니다.", appName, engineName, procInstanceId);
		logger.log(Level.INFO, retVal);
		return retVal;
	}
	
	public String[] createSOEvent(String eqpID, String message) {
		String[] vals = {"",""};
		logger.log(Level.INFO,String.format("장비[%s] 이벤트 메시지 수신 [%s] ", eqpID, message));
		return vals;
	}
	
	public String createEQPEvent(int EQPseqno) {
		return String.format("[%d][%s] 장비 메시지를 전달하였습니다.", EQPseqno, "createEQPEvent");
	}
	

	
	public String sayHello(String eqpID, String message) {		
		String OS = System.getProperty("os.name");
		String val = String.format("[%s][%s]test messageg 로그가 어떻게 남는지 확인이 필요하다. ",OS, eqpID);
		logger.log(Level.INFO, val);
		
		//Thread.currentThread().setContextClassLoader(LoadDynamic.class.getClassLoader());						
		// bedb.registerDataSource("oracle", "oracle", "edward", "system", "tkfkd1104", "jdbc:oracle:thin:@192.168.0.20:1521/XEPDB1",  null, 50);	
		return val;
	}

}


