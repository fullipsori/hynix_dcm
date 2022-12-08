package com.skhynix.neesp;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.skhynix.common.StringUtil;
import com.skhynix.controller.BusinessLogic;
import com.skhynix.controller.MessageRouter;
import com.skhynix.manager.BusinessManager;
import com.skhynix.manager.DynaClassManager;
import com.skhynix.manager.MetaDataManager;
import com.skhynix.manager.ResourceManager;
import com.skhynix.model.message.BaseMsgModel;
import com.skhynix.model.session.BaseSessModel;
import com.skhynix.model.session.EmsSessModel;
import com.skhynix.model.session.EmsSessModel.SESS_MODE;
/*
 * Refactoring을 통해서 정리할 내용들 - 초기 개발 시 기능 점검용 (원재일)
 */
import com.skhynix.neesp.ConfigManager.ConfigManager;
import com.skhynix.neesp.MessageRouterTest.*;
import com.skhynix.neesp.log.LogManager;
import com.skhynix.neesp.log.NEESPLogger;
import com.skhynix.neesp.log.Utils;
// merging 과정 중 정리 필요 - 원재일
import com.skhynix.neesp.util.*;



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
	private final ResourceManager resourceManager = ResourceManager.getInstance();
	private final MetaDataManager metaDataManager = MetaDataManager.getInstance();

	public BaseProxy() {
		// 생성자
		System.out.println("/// BaseProxy called");
		LogManager.getInstance().startMonitor();
	}
	
	@SuppressWarnings("unchecked")
	public String openSession(String joinType, String param) {
		/**  channelInfo format
		 * [INBOUND,EMS,ems#01,tcp://192.168.232.142:7222,QUEUE.FAB1.AREA1.PHOTO.EQP1,AUTO_ACK
		 * |OUTBOUND,EMS,ems@02,tcp://192.168.232.142:7223,QUEUE.FAB1.AREA1.PHOTO.EQP1,AUTO_ACK]
		 */
		
		if(StringUtil.isEmpty(joinType) || StringUtil.isEmpty(param)) return "";
		
		String serverUrl;
		String jsonString;

		if(joinType.equals("message-ems")) {
			String[] tokens = param.split(",");
			EmsSessModel sessModel = new EmsSessModel();
			sessModel.role = (tokens[0].equalsIgnoreCase("OUTBOUND"))?  "sender" : "receiver"; 
			// fullip : ip가 fix 되어서 들어온다. 
			if(sessModel.role.equals("receiver")) {
				// sessModel.serverUrl = tokens[3];
				sessModel.serverUrl = "localhost:7222";
				sessModel.sessionMode = SESS_MODE.EXPLICIT_CLIENT.modeType;
			}else {
				// sessModel.serverUrl = tokens[3];
				sessModel.serverUrl = "localhost:7223";
				sessModel.sessionMode = SESS_MODE.AUTO.modeType;
			}
			sessModel.queueName = tokens[4];
			serverUrl = sessModel.serverUrl;
			jsonString = StringUtil.objectToJson(sessModel);
		}else {
			Map<String, Object> params = null;
			params = StringUtil.jsonToObject(param, Map.class);
			serverUrl = (String)params.get("serverUrl");
			jsonString = StringUtil.objectToJson(params);
		}

		System.out.println("join:" + joinType +  " jsonString:" + jsonString);
		if(joinType.startsWith("message")) {
			String handle = messageRouter.openSession(joinType, serverUrl, jsonString);
			return handle;
		}else if(joinType.startsWith("resource")) {
			return resourceManager.openSession(joinType, serverUrl, jsonString);
		}else {
			return "error:" + joinType;
		}
	}
	
	public void closeSession(String handle) {
		String[] tokens = handle.split(BaseSessModel.defaultDelimiter);
		if(StringUtil.isEmpty(tokens[0])) {
			return;
		}
		if(tokens[0].equals("message")) {
			messageRouter.closeSession(handle);
		}else if(tokens[0].equals("resource")) {
			resourceManager.closeSession(handle);
		}else {
		}
	}
	
	public void testAS(String loadType) {
		/**
		TestDTO dto = new TestDTO();
		dto.key = "111";
		dto.value1 = "test value1";
		dto.value2 = "test value2";
		boolean res = metaDataManager.createMetaData(loadType, "hynix_table", dto);
		if(res) {
			TestDTO dto2 = new TestDTO();
			metaDataManager.retrieveMeta(loadType, "hynix_table", Pair.of("key", "111"), dto2);
			System.out.println("result: " + dto2.key + " value1:" + dto2.value1 + " value2:" + dto2.value2);
		}else {
			System.out.println("failed");
		}
		**/
	}
	
	@SuppressWarnings("unchecked")
	public void sendMessage(String handle, String data, String jsonProperties) {
		String[] tokens = handle.split(BaseSessModel.defaultDelimiter);
		if(StringUtil.isEmpty(tokens[0])) {
			return;
		}
		if(tokens[0].equals("message")) {
			Map<String,String> properties = null;
			if(StringUtil.isNotEmpty(jsonProperties)) {
				properties = (Map<String,String>)StringUtil.jsonToObject(jsonProperties, Map.class);
			}
			messageRouter.sendMessage(handle, data, properties);
		}else { }
	}
	
	public String receiveBodyMessage(String handle, long waitTimeInMillis) throws Exception {
		String[] tokens = handle.split(BaseSessModel.defaultDelimiter);
		if(StringUtil.isEmpty(tokens[0])) {
			return "";
		}
		if(tokens[0].equals("message")) {
			return Optional.ofNullable(messageRouter.receiveMessage(handle, waitTimeInMillis)).map(BaseMsgModel::getMessage).orElse("");
		}else {
			return "";
		}
	}

	public String receiveMessageWithProperties(String handle, long waitTimeInMillis) throws Exception {
		String[] tokens = handle.split(BaseSessModel.defaultDelimiter);
		if(StringUtil.isEmpty(tokens[0])) {
			return "";
		}
		if(tokens[0].equals("message")) {
			return Optional.ofNullable(messageRouter.receiveMessage(handle, waitTimeInMillis)).map(BaseMsgModel::toJson).orElse("");
		}else {
			return "";
		}
	}
	
	@SuppressWarnings("unchecked")
	public String sendAndReceiveBody(String handle, String data, String jsonProperties, String replyQueue, String selector, long waitTimeInMillis) {
		String[] tokens = handle.split(BaseSessModel.defaultDelimiter);
		if(StringUtil.isEmpty(tokens[0])) return ""; 
		if(tokens[0].equals("message")) {
			Map<String,String> properties = null;
			if(StringUtil.isNotEmpty(jsonProperties)) {
				properties = (Map<String,String>)StringUtil.jsonToObject(jsonProperties, Map.class);
			}
			return Optional.ofNullable(messageRouter.sendAndReceive(handle, data, properties, replyQueue, selector, waitTimeInMillis)).map(BaseMsgModel::getMessage).orElse("");
		}else {
			return "";
		}

	}

	public void confirmMessage(String handle) {
		String[] tokens = handle.split(BaseSessModel.defaultDelimiter);
		if(StringUtil.isEmpty(tokens[0])) {
			return;
		}
		if(tokens[0].equals("message")) {
			messageRouter.confirmMessage(handle);
		}else {
		}
	}
	
	public void sendMessageToTargets(String handles, String message) {
		String[] targets = handles.split(BaseSessModel.defaultDelimiter);
		messageRouter.sendAsyncTo(targets, message);
	}

	/** 
	 * Message route handle 들을 Map type 으로 전달하도록 하고, 나중에 다시 변경하도록 한다.
	 * @param eqpId
	 * @param appNodeName
	 * @param eventType
	 * @param message
	 * @return
	 */
	public String doBusiness(String eqpId, String appNodeName, String eventType, String message, String emsHandle, String kafkaHandle, String ftlHandle) {
		try {
			Map<String,String> handles = new HashMap<>();
			if(StringUtil.isNotEmpty(emsHandle)) handles.put("ems", emsHandle);
			if(StringUtil.isNotEmpty(kafkaHandle)) handles.put("kafka", kafkaHandle);
			if(StringUtil.isNotEmpty(ftlHandle)) handles.put("ftl", ftlHandle);
			return businessLogic.doBusiness(eventType, message, handles, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}
	
	public boolean loadJar(String className, String classDomain, String classPath) {
		return dynaClassManager.loadJar(className, classDomain, classPath);
	}
	
	public boolean unloadJar(String className, String classDomain) {
		return dynaClassManager.unloadJar(className, classDomain);
	}
	
	
	/*
	 * 초기 기능 테스트용 개발 파트 - 통합 중 변경 예상되는 영역
	 * 
	 */
	public String[] moduleTest(String command, String parameter, String parameter2, String parameter3, String parameter4) {
		
		weightBasedRandomValue();
		String eqpId = "EQP1";
		System.out.println("/// 최초 생성- 장비 생성시점에 같이 생성한다. NEESPLogger: " + eqpId);		
		eqpLogger = new NEESPLogger(eqpId, "sw-node-1", LogManager.getInstance().getLogger());
		
		String[] retVals = {"",""};
		
		System.out.println("1.moduleTest");
		eqpLogger.syslog(parameter4,"module-test","TRACE");
		System.out.println("2.moduleTest - sysLogOff");
		eqpLogger.syslogOff();
		eqpLogger.syslog("sys log 외부로 내보는 것을 종료하였습니다. - KAFKA로 출력이 되나요.","module-test","TRACE");
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
			eqpLogger.syslog("메시지를 보내는데 큐를 이용해서 버퍼링 후 전송합니다.. - KAFKA로 출력이 되나요. ("+(i+1)+")","module-test","TRACE");
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
		
		/*
		 * 테스트용 메시지 생성기
		 */
		sb.append("{");
		sb.append(String.format("\"eqpId\":\"%s\",",eqpId));
		sb.append(String.format("\"eventType\":\"%s\",",rc.next()));
		sb.append(String.format("\"msgId\":\"%s\",",String.format("%s-%d", eqpId, msgId)));
		sb.append(String.format("\"messageBody\":\"%s\",",message));
		sb.append(String.format("\"reserved\":\"%s\"","reserved"));
		sb.append("}");
		
		/*
		 *  장비를 위한 큐 명명 유사 규칙으로 생서한다.
		 */
		retVals = new bwReturnValues().retVal1(String.format("QUEUE.FAB1.AREA1.PHOTO.%s", eqpId), sb.toString());
		System.out.printf("[%s]\n", retVals[1]);
		logger.log(Level.INFO,retVals[1]);
		
		return retVals;
	}
	
	/*
	 * NEESPLogInfo- Transaction Log 사용을 위한 함수 호출
	 * Transaction Log 관련 체크 함수 모듬 - TRACE TRX 쫓기위한 로그
	 */
	
	public String[] trxStartLog(String eqpId, String eventType, String msgId, String message, String log, String jobType) {
	/*
		 * 테스트용으로 gtxnId와 extnId를 임의로 생성한다.
	 */
		String gxtnId = String.format("gtxnId-%d", System.nanoTime());
		String extnId = String.format("extnId-%s", Utils.getInstnace().yyyymmdd_hhmmssSSS(Utils.currentTime) );		
	
		SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getNEESPLogger().logTrxStart(log, eventType, gxtnId, extnId, msgId, jobType);
		return new bwReturnValues().retVal1("succeed-trx-log", "trxStartLog를 출력하였습니다.");
	}
	
	public String[] trxProcLog(String eqpId, String log, String jobType) {		
		SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getNEESPLogger().logTrxProc(log, jobType);		
		return new bwReturnValues().retVal1("succeed-trx-log", "trxProcLog를 출력하였습니다.");
	}
	
	public String[] trxEndLog(String eqpId, String log, String jobType) {
		SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getNEESPLogger().logTrxEnd(log, jobType);
		return new bwReturnValues().retVal1("succeed-trx-log", "trxStartLog를 출력하였습니다.");
	}
	
	/*
	 * NEESP-SO & NEESP-SW 하트비트 체크를 위한 모니터링 체크 함수 모음
	 */
	public String[] checkEqpsStatus() {
		String[] retVals = null;		
		String eqpIds = EQPInfoManager.getInstance().checkEqpsStatus();
		System.out.printf("장비 상태 체크: %s\n", eqpIds);
		return new bwReturnValues().retVal1("succeed-check-eqps-status", eqpIds);
	}
	
	public String[] checkSWWorkersStatus() {
		String[] retVals = null;		
		String swWorkerIds = SWWorkerInfoManager.getInstance().checkSWWorkersStatus();
		System.out.printf("장비 상태 체크: %s\n", swWorkerIds);
		return new bwReturnValues().retVal1("succeed-check-sw-workers-list", swWorkerIds);
	}
	
	
	/*
	 * NEESP-SO 수행에 필요한 함수	 * 
	 */
    public String[] addNewSWNode(String appNodeName, String procInstanceId) {
        String logMsg = String.format("[%s][%s] 새로운 SWNode Id를 추가하였습니다.", appNodeName, procInstanceId);
        System.err.printf("%s\n", logMsg);
        return SWNodeInfoManager.getInstnace().addNewSWNode(appNodeName, procInstanceId);
    }
    
    public String[] initializeSWNode(String appNodeName) {
        String[] retVals = {"",""};
        String logMsg = String.format("[%s] SWNode Id가 새롭게 기동되었습니다. [SO]", appNodeName);
        System.err.printf("%s\n", logMsg);
        // retVals = SWNodeInfoManager.getInstnace().initializeSWNode(appNodeName);
        return retVals;      
    }
    
    public String[] releaseSWNode(String appNodeName) {
        String[] retVals = {"",""};
        String logMsg = String.format("[%s] SWNode Id를 제거하였습니다. [SO]", appNodeName);
        System.err.printf("%s\n", logMsg);
        return SWNodeInfoManager.getInstnace().releaseSWNode(appNodeName);
    }    
    
    public String[] initSWWorker(String eqpId, String inboundQueue) {
        String[] retVals = {"",""};        
        String logMsg = String.format("[%s] 새로운 SW Worker를 초기화하였습니다. [SO][%s]", eqpId, inboundQueue);
        System.err.printf("%s\n", logMsg);
        EQPInfoManager.getInstance().printEQPInfo(eqpId,"init-new-worker");
        return EQPInfoManager.getInstance().initEQPInfo(eqpId, inboundQueue);
    }
    
    public String[] addNewSWWorker(String eqpId, String appNodeName, String procInstanceId) {
        String[] retVals = null;        
        String logMsg = String.format("[%s][%s][%s] 새로운 SW Worker를 추가하였습니다. [SO]", eqpId, appNodeName, procInstanceId);
        retVals = SWNodeInfoManager.getInstnace().addNewSWWorker(eqpId, appNodeName);
        if(retVals[0].contains("succeed")) {
        retVals = EQPInfoManager.getInstance().changeEQPInfoWhenCreated(eqpId, appNodeName, procInstanceId, "active","ready");
        EQPInfoManager.getInstance().printEQPInfo(eqpId,"add-new-worker");
        }
        return retVals;
    }
    
	public String[] updateSWWorker(String eqpId, String appNodeName, String procInstanceId, String workerStatus) {
		/*
		 * init-sworker request from SWNode
		 * SWNodeInfoManager의  SWNodeInfo에 eqpIds 추가
		 */
		String[] retVals = null;
		long step =0;
		logger.log(Level.INFO, String.format("[%s]",eqpId));
		
		try {		
			// SWNodeInfo 및 장비 상태 정보 갱신 - 재기동 여부를 확인하고 거기에 맞는 활동을 수행한다.			
			SWNodeInfoManager.getInstnace().addNewSWWorker(eqpId, appNodeName);
			step++;
			retVals = EQPInfoManager.getInstance().changeEQPInfoWhenCreated(eqpId, appNodeName, procInstanceId, "active", workerStatus);
			step++;
			// 상태 정보 추력
			EQPInfoManager.getInstance().printEQPInfo(eqpId,"update-sw-worker-status");
			step++;
			SWNodeInfoManager.getInstnace().getSWNodeInfo(appNodeName).printSWNodeInfo("success-init-swworker-from-swn");
			step++;
		} catch (Exception ex) {
			return new bwReturnValues().retVal1("error", String.format("Exception Error - 문제가 발생하였습니다. step count[%d]",step));
		}
		
		return retVals;
	}
    
    public String[] releaseSWWorker(String eqpId, String appNodeName) {
        String[] retVals = {"",""};        
        String logMsg = String.format("[%s][%s] 새로운 SW Worker를 노드에서 제거하였습니다. [SO]", eqpId, appNodeName);
        retVals = SWNodeInfoManager.getInstnace().releaseSWWorker(eqpId, appNodeName);
        if(retVals[0].contains("succeed")) {
        retVals = EQPInfoManager.getInstance().changeEQPInfoWhenReleased(eqpId, "", "", "deactive","stopped");
        	EQPInfoManager.getInstance().printEQPInfo(eqpId,"released-sw-worker");
        }
        return retVals;
    }
    
    public String[] getSpawnRequestInfo(String eqpId) {
    	String[] retVals = null;    	
    	String queueName = EQPInfoManager.getInstance().getEQPInfo(eqpId).getInboundQueue();
    	String swnodeId = EQPInfoManager.getInstance().getEQPInfo(eqpId).getSwnodeId();
    	System.err.printf("/// [%s][%s][%s] getSpawnRequestInfo: SWN 초기화에 따른 SWWorker 생성 요청\n", eqpId, swnodeId, queueName);
    	retVals = this.allocateChannelInfo(eqpId, swnodeId, queueName);    	
    	System.err.printf("/// allocated and Request Command: %s \n", retVals[2]);    	
        return retVals;
    }

    public String[] handleGetMsgTimeoutEvent(String eqpId, String appNodeName, String timeoutCount, String procInstanceId ) {
        String[] retVals = {"",""};
        String logMsg = String.format("[%s][%s] SWWorker에서 타임아웃 이벤트가 발생하였습니다.[%s][SO]", eqpId, appNodeName, timeoutCount);

        // retVals = SWNodeInfoManager.getInstnace().releaseSWWorker(eqpId, appNodeName);
        if(EQPInfoManager.getInstance().getEQPInfo(eqpId) != null) {
        	EQPInfoManager.getInstance().getEQPInfo(eqpId).setCountTimeoutEvent(Long.parseLong(timeoutCount));
        	EQPInfoManager.getInstance().printEQPInfo(eqpId, "check-info-by-timeout-event");
        } else {
        	/*
        	 *  기본 시작 순서: SO 먼저 기동 => SWN 기동
        	 *  운영 환경 상: SO가 죽은 경우 => SWN은 실행 중인 상태 => 이 경우  
        	 *  SO가 나중에 기동되었다는 의미 - 이 경우 동기화 작업을 위하여
        	 *  SWNode 정보 설정을 새롭게 하고, 필요한 정보들을 만드나.          	 
        	 */        	
        	this.addNewSWNode(appNodeName, procInstanceId);
        	this.initSWWorker(eqpId, ""); // 어떻게 받을 것인지 확인한다. - EDWARD-WON: InboundQueue명을 찾아서 만들어준다. 꼭 수정필요 12/5
        	
        	// 실제 매니저에 설정한다.
        	System.out.printf("/// [%s][%s] NEESP-SO가 NEESP-SW에 SW-worker 실행 중에 재기동된 상태\n", eqpId, appNodeName);
        	retVals = SWNodeInfoManager.getInstnace().addNewSWWorker(eqpId, appNodeName);
        	if(retVals[0].contains("succeed")) {
        		retVals = EQPInfoManager.getInstance().changeEQPInfoWhenCreated(eqpId, appNodeName, procInstanceId, "active","ready");        	
        		EQPInfoManager.getInstance().printEQPInfo(eqpId,"add-swnode-and-worker-by-TimeoutEvent [완료]");
        	} else {
        		System.err.printf("/// [%s][%s] 오류가 발생하였습니다.", retVals[0], retVals[1]);
        	}
        }
        return retVals;
    }

	public String[] checkEqpId(String queueName){
		String[] retVals = {"",""};
		/*
		 * 큐 명에서 장비 ID를 분리한다.
		 * 1) EQP 마스터에 등록된 장비인지 확인한다.	 
		 * 2) 등록되지 않은 장비인 경우 - 장비 마스터 DB 조회 => 조회 시에도 존재하지 않는 경우 => 신규 미등록 장비 알람을 보내준다.
		 * 3)	 
		 */
		 int index = queueName.lastIndexOf(".");
		 retVals[1] = queueName.substring(index+1);
		 System.err.printf("/// checkEqpId [%s] 큐 명에서 [%s] eqpId를 분리하고 확인하였습니다.", queueName, retVals[1]);		 
		 return retVals;
	}
	
	public String[] allocatSWNodeForNewEqp(String eqpId) {
		String[] retVals = null;		
        retVals = SWNodeInfoManager.getInstnace().allocateSWnode(eqpId, "round-robin");
        
        String code = retVals[0];
        String swnodeId = retVals[1];
        
		EQPInfoManager.getInstance().setSWNodeId(eqpId, swnodeId, "wait-worker-ready");
		EQPInfoManager.getInstance().printEQPInfo(eqpId, "allocated-swnode");
		
		logger.log(Level.INFO,String.format("[%s] 장비 할당 요청 이벤트 메시지 발신 [%s]", eqpId, swnodeId));
		
		return new bwReturnValues().retVal2(code, String.format("queue.so.%s.ctl", swnodeId), swnodeId);
	}
		
	public String[] allocateChannelInfo(String eqpId, String swnodeId, String queueName) {
        
        /*
         * 진짜 할당을 위해서는 설정 정보를 관리해야 한다. - Database 설정 정보 관리 - Configuration Manager
         *   
         * String channelInfo = ChannelInfoManager.getChannelInfo(eqpId);
         * = 전달 포맷에 대해서는 정의가 필요합니다. JSON 형식으로 할 것인지 아래와 같이 정의된 구분자 타입으로 할지를 결정할 필요가 있다.
         */
        String strAckMode = "AUTO_ACK";
        /*
         * 임시 방편으로 만들어진 것임 - 기설정된 EMS INBOUND, OUTBOUND 정보를 전달한다. - 가용한 자원을 할당하여 처리하게 만드는 것을 목표로 한다.
         */
        String channelInfo = ConfigManager.getInstance().allocateChannelInformation(eqpId, swnodeId, queueName, strAckMode);
        
        System.err.printf("///////////////////////////////////////////////////////////////\n");
        System.err.printf("/// 할당된 채널 인/아웃바운드 채널 정보 : %s\n", channelInfo);
        System.err.printf("///////////////////////////////////////////////////////////////\n");
        
		String destinationQueueName = String.format("queue.so.%s.ctl", swnodeId); // 장비 제어용 토픽 토픽 진행
    	String req = new SORequestCommand().make("spawn-request", eqpId, swnodeId, channelInfo, String.format("[%s] 정상적으로 채널정보가 할당되었습니다.", eqpId));

        String[] retVals = new bwReturnValues().retVal2("succeed-allocated-swn", destinationQueueName, req);
		logger.log(Level.INFO,String.format("[%s] 장비 할당 요청 이벤트 메시지 발신 [%s]", eqpId, retVals[1]));
		
		return retVals;
	}
	
	/*
	 * stop-request: SWN 내 SW-worker(장비 이벤트 처리)를 종료시키기 위한 명령어
	 */
	public String[] stopSWWorker(String eqpId, String reqCommand) {
		
		EQPInfo eqpInfo = null;
		
		if((eqpInfo =EQPInfoManager.getInstance().getEQPInfo(eqpId, reqCommand)) == null) {
			return new bwReturnValues().retVal1("error-not-found-eqpinfo", String.format("주어진 장비 Id [%s] 에 해당하는 장비 정보가 없습니다.", eqpId));
		}
		
		// EQPInfo 장비 정보 객체가 존재하는 경우 명령를 생성한다.
		String swnodeId = eqpInfo.getSwnodeId();
		String procInstanceId = eqpInfo.getWorkerId();
		
		String destinationQueue = String.format("queue.so.%s.ctl", swnodeId);
		String req = new SORequestCommand().make("stop-request", eqpId, swnodeId, procInstanceId, String.format("\"parameter4\":\"%s\"","SWN 관련 정리 작업을 부탁드립니다."));
		
		return new bwReturnValues().retVal2("stop-request", destinationQueue, req);
	}
	
    /*
     * NEESP-SWN 수행에 필요한 함수 목록
     */
    public String[] checkSWorkerInfo(String command, String parameter, String parameter2, String parameter3, String parameter4) {
        String[] retVals = null; 
        String eqpId = parameter;
        String swnodeId = parameter2;
        
        if("spawn-request".equalsIgnoreCase(command)) {        	
            boolean bExist = SWWorkerInfoManager.getInstance().initSWWorkerInfo(eqpId, swnodeId);            
            if(bExist) {
            	// 이미 기존에 정보를 가기오 있는 경우 판단한다. 정보만 있는지 아니면 실행이 정지되어 있는 상태인지를
                SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setSwnodeId(swnodeId);
                SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setWorkerStatus("ready");
                
                if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getWorkerStatus().equalsIgnoreCase("stop") == false) {
                    retVals = new bwReturnValues().retVal3("use-exist-worker-thread", String.format("[%s] 이미 실행 중인 작업자가 있습니다.", eqpId), eqpId, swnodeId);
                    System.out.println(retVals[1]);
                } else {
                    retVals = new bwReturnValues().retVal3("need-to-spawn-worker-thread", "기존에 생성한 정보가 있느나 정지되어서 삭제되어 있는 상태", eqpId, swnodeId);
                    System.out.printf("[%s] Thread Spawning 필요 [%s]\n", eqpId, retVals[0]);
                }
                
                SWWorkerInfoManager.getInstance().printSWWorkerInfo(eqpId,"check-swworker-info");
                
            } else {
            	retVals = new bwReturnValues().retVal3("need-to-spawn-worker-thread", "기존에 정보가 없는 경우에 해당합니다.", eqpId, swnodeId);
            }            
          
        } else if("stop-request".equalsIgnoreCase(command)) {
            if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId) != null) {
                SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setSORequest("stop-request");
                SWWorkerInfoManager.getInstance().printSWWorkerInfo(eqpId,"set-stop-request");
                retVals = new bwReturnValues().retVal3("succeed-set-stop-request", "SW-Worker 정지 작업을 위한 값 설정 완료", eqpId, swnodeId);
                System.err.printf("/// [%s] 작업자 정지 요청\n", eqpId);
            } else {
                System.err.printf("/// [%s] 장비를 위한 작업자가 없습니다.\n", eqpId); 
                retVals = new bwReturnValues().retVal3("error-not-found-swworkerinfo",String.format("[%s]에 해당하는 SWWorkerInfo를 찾을 수 없습니다.", eqpId), eqpId, swnodeId);
            }
        }
        
        return retVals;
    }
    
    public String[] prepareAndCheckDup(String eqpId, String messageId) {

        if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId)==null) {
             return new bwReturnValues().retVal1("error-swworker-not-found", String.format("[%s] 찾을 수 없습니다. messageId %s", eqpId, messageId));
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
        
        String msgProcStage = SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getMsgProcStage();
        String logMsg = String.format("/// [%s] 메시지 프로세싱 상태는 [messageId %s][%s]", eqpId, messageId, msgProcStage);
        
        System.err.println(logMsg);
    
        return new bwReturnValues().retVal1(msgProcStage, logMsg);
    }
        
     public String[] doBusiness(String eqpId, String appNodeName, String eventType, String messge) {

        if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId)==null) {
             return new bwReturnValues().retVal1("error-swworker-not-found-do-business", String.format("[%s][%s] WorkerInfo가 존재하지 않습니다.", eqpId, eventType));
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
        return new bwReturnValues().retVal1("succeed-do-business", String.format("[%s][%s] 이벤트 작업이 정상적으로 마무리되었습니다.", eqpId, eventType));
    }
     
    public String[] checkNextStepWhenGetTimeout(String eqpId, String appNodeName, String procInstanceId) {
    	String[] retVals = null;
    	
		/*
		 * 1. SWWorkerInfo 상태 체크: 
		 * 2. 요청 메시지 항목이 무엇인지 확인하고 작업을 수행한다.
		 * 3. 작업이 완료되었다면 결과 메시지를 만든다.
		 */
        if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId) == null) {           
           retVals = new bwReturnValues().retVal1("error-not-found-swworker", String.format("[%s] 장비의 SWWorkerInfo 정보가 없습니다. 동기화를 위한 메시지가 필요합니다.", eqpId));
        } else if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getSORequest().equalsIgnoreCase("stop-request")) {
        
    		String req = new SORequestCommand().make("release-swworker", eqpId, appNodeName, procInstanceId, String.format("\"parameter4\":\"%s\"", "SWNode Worker가 삭제되었습니다. SO내 EQPInfo 정보의 변경이 필요합니다."));
    		retVals = new bwReturnValues().retVal1("succeed-release-swworker", req);
        
        } else {
           retVals = new bwReturnValues().retVal1("error-no-match-command", String.format("SORequest [%s]에 매칭되는 command가 없습니다.",  SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getSORequest()));
        }
		
		logger.log(Level.INFO,String.format("[%s][%s]  checkNextSetpWhenGetTimeout 발생했을 때 - 작업을 마무리 하겠습니다.", eqpId, procInstanceId));
		
		return retVals;
 	}
    
    public String[] makeTimeoutEventMsg(String eqpId, String appNodeName, String procInstanceId) {
    	/*
    	 * ReceiveGetTimeout 발생 횟수를 전달한다.
    	 */
    	long timeoutCount = SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).increaseTimeoutEventCount();
		
		String req = new SORequestCommand().make("getmsg-timeout", eqpId, appNodeName, String.format("%d", timeoutCount),procInstanceId);		
		return  new bwReturnValues().retVal1("succeed-created-timeout-msg", req);
	}
		
	public String[] initializeSWN(String appNodeName, String applicationName, String procInstanceId ) {
		
		String ret = String.format("[%s][%s][%s] 정상적으로 초기화 되었습니다.", appNodeName, applicationName, procInstanceId);	
		
		/*
		 * 초기화 작업을 수행합니다. 작업이 성공적으로 끝나면  해당 이벤트를 보내줍니다.
		 * 오류가 발생시 - shutdown 하고 생성 오류 메시지를 만들어서 보내주고 정리한다.
		 */
		String req = new SORequestCommand().make("init-swnode", appNodeName, applicationName, procInstanceId, ret);	
		return  new bwReturnValues().retVal1("succeed-initilized-swnode", req);
	}
		
	public String[] releaseSWN(String appNodeName, String applicationName, String procInstanceId) {
		
		String ret = String.format("[%s][%s][%s] 모든 정리작업을 수행하였습니다. - 마무리 처리 부탁합니다.", appNodeName, applicationName, procInstanceId);
		/*
		 * SWN 노드를 릴리즈 하고 나서 SO에게 알려준다.
		 */
		String req = new SORequestCommand().make("release-swnode", appNodeName, applicationName, procInstanceId, ret);	
		return  new bwReturnValues().retVal1("succeed-initilized-swnode", req);
	}
		
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/*
	 * channelInfo = parameter3 형식 - 구분자 사용:  
	 * 
	 * 예)"parameter3" = "src,EMS,ems#01,tcp://192.168.232.142:7222,EQP1|tar,EMS,ems@02,tcp://192.168.232.142:7223,EQP1" 
	 * 
	 */
	private void printCheckValue(String[] inbound, String[] outbound) {
		System.err.println("-------------------------------------------------");
		for(int i=0; i<inbound.length; i++){
			System.err.printf("[%d][%s]\n", i, inbound[i]);
		}
		System.err.println("-------------------------------------------------");		
		for(int i=0; i<outbound.length; i++){
			System.err.printf("[%d][%s]\n", i, outbound[i]);
		}
		System.err.println("-------------------------------------------------");
	}
		
	public String initializeChannelInfo(String eqpId, String appNodeName, String channelInfo){
		
		String retVal = "";
		
		try {			
			System.err.printf("채널 정보 보여주기 [%s][%s][%s]￦n", eqpId, appNodeName, channelInfo);
			
			String[] inbound = channelInfo.split("\\|")[0].split(",");
			String[] outbound = channelInfo.split("\\|")[1].split(",");
			
			printCheckValue(inbound, outbound);
			
			/*
			 *  신규 서버 정보를 생성하고  채널 정보 관리자에 등록한다.
			 */
			ChannelInfoManager.getInstance().setChannelServerInfo(new ChannelServerInfo(inbound));
			ChannelInfoManager.getInstance().setChannelServerInfo(new ChannelServerInfo(outbound));
			
			/*
			 *  MessageRouter에 저장할 Router정보 저장 - session 기반으로 연결에 필요한 내용을 정리한다.
			 *  session객체를 이용하여  destination과 topic 혹은 queue를 생성한다.
			 */

			EMSMessageRouter inboundEMSCI = new EMSMessageRouter(inbound);
			EMSMessageRouter outboundEMSCI = new EMSMessageRouter(outbound);
			
			/*
			 * MessageRouter에 EqpId와 In/Outbound EMS Channel Info를 등록한다.
			 */
			MessageRouterTest.getInstance().setInOutChannelInfo(eqpId, inboundEMSCI, outboundEMSCI);
			
		}catch(Exception ex) {
		
		}
		return retVal;
	}
	
	public String[] receiveMessage(String eqpId, long waitTimeOut) {		
		String[] retVals = null;		
		try {
			String receivedMessage = MessageRouterTest.getInstance().consumeMessage(eqpId, waitTimeOut);
			retVals = new bwReturnValues().retVal1("succeed-received-message", receivedMessage);
		} catch(Exception ex) {
			System.err.println(ex.getMessage());
			retVals = new bwReturnValues().retVal1("error-general-exception", ex.getMessage());
		} catch(ReceivedTimeoutException er) {
			System.err.println(er.getMessage());
			retVals = new bwReturnValues().retVal1("error-received-timeout", er.getMessage());
		}
		
		return retVals;
	}
		
	public String[] produceMessage(String eqpId, String message) {
		try {
			MessageRouterTest.getInstance().produceMessage(eqpId, message);
			return new bwReturnValues().retVal1("succeed-send-message", "메시지 전송을 정상적으로 마무리 하였습니다.");
		} catch(Exception ex) {
			return new bwReturnValues().retVal1("error-send-message", ex.getMessage());
	}
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public String[] initSWWorker(String eqpId, String appNodeName, String applicationName, String procInstanceId, String channelInfo) {
		
		String[] retVals = null;
		/*
		 * 1. 초기화 작업을 진행합니다.
		 */		
		String ret = initializeChannelInfo(eqpId, appNodeName, channelInfo);
		
		if(ret.contains("error")) {
    		String req = new SORequestCommand().make("error-init-swworker-channel-setup", eqpId, appNodeName, ret, String.format("[%s] 채널 형성 중 오류 다음과 같은 오류가 발생하였습니다. [%s]", eqpId));			
			return new bwReturnValues().retVal1(ret, req);
		}
		
		if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId) != null) {
			
            SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setSwnodeId(appNodeName);
            SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setWorkerId(procInstanceId);
            SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setWorkerStatus("ready");
            SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setMessageId("n/a");
            SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setMsgProcStage("n/a");
            SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getSWWorkerInfo("init-swworker");
            
    		String req = new SORequestCommand().make("init-swworker", eqpId, appNodeName, procInstanceId, String.format("[%s] 정상적으로 초기화 되었습니다.", eqpId));
            retVals = new bwReturnValues().retVal1("succeed-init-swworker", req);

         } else {
           // 해당하는 장비의 eqpId가 없는 경우
   		   String req = new SORequestCommand().make("error-init-swworker-not-found", eqpId, appNodeName, procInstanceId, String.format("[%s] SWWorker 생성에 실패하였습니다. [error: not found swworker]", eqpId));
   		   retVals = new bwReturnValues().retVal1("error-init-swworker-not-found", req);
         }
		
		logger.log(Level.INFO,String.format("[%s][%s] 장비를 위한 SW-Worker 초기화 작업 수행을 완료하였습니다. [%s]", eqpId, procInstanceId, retVals[0]));
		
		return retVals;
	}
	
	public String[]  releaseSWWorker(String eqpId, String appNodeName, String applicationName, String procInstanceId) {
		
		String ret = String.format("%s|%s|%s|%s| 장비 큐 처리용 작업자가 정리되었습니다.", eqpId, appNodeName, applicationName, procInstanceId);
		String code = "";
		
        if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId) != null) {
           SWWorkerInfoManager.getInstance().releaseSWWorkerInfo(eqpId);          
           code = "succeed-removed-swworker";
        } else {
           code = "error-not-found-swworker";
           ret = String.format("%s|%s|%s|%s| 장비 큐 처리용 작업자를 찾을 수 없습니다.", eqpId, appNodeName, applicationName, procInstanceId);
        }
		
		String req = new SORequestCommand().make("release-swworker", eqpId, appNodeName, procInstanceId, ret);
		return new bwReturnValues().retVal1(code, req);
	}
	
	public String[] updateSWWorkerInfo(String eqpId, String appNodeName, String applicationName, String workerStatus) {
		/*
		 * updateSWWorkerInfo 필요할 때 코드를 추가한다.
		 */
		
		return new bwReturnValues().retVal1("succeed-update-swworkerinfo", String.format("[%s][%s] SWWorkerInfo정보를 업데이트했습니다.", eqpId, workerStatus));
	}
	
	public String[] readyToNext(String eqpId, String appNodeName, String applicationName, String procInstanceId) {
    	String[] retVals = null;
		
        if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId) != null) {
        	
        	if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getSORequest().equalsIgnoreCase("stop-request")) {        		
        		
        		String req = new SORequestCommand().make("release-swworker", eqpId, appNodeName, procInstanceId, String.format("[%s][%s] SW-worker를 정리합니다.", eqpId, appNodeName));        		
        		retVals = new bwReturnValues().retVal1("stop-request", req);
        		
        	} else {	           
        		
        		retVals = new bwReturnValues().retVal1("succeed-ready-to-next", String.format("[%s][%s] 메시지 처리가 잘 수행하고 다음 작업을 수행합니다.", eqpId, SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getMessageId()));
        		
        		SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setWorkerStatus("ready");
        		SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setMsgProcStage("ready");
        		SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).setMessageId("wait-new-event-msg");
        	}
        } else {
        	
           retVals = new bwReturnValues().retVal1("error-not-found-swworker", String.format("[%s][%s] 메시지 처리가 잘 수행하고 다음 작업을 수행합니다.", eqpId, appNodeName));
        }
		
		logger.log(Level.INFO,String.format("[%s][%s] 다음 처리를 위하여 준비합니다. ReadyToNext: [다음 작업: %s]", eqpId, procInstanceId, retVals[1]));		
		SWWorkerInfoManager.getInstance().printSWWorkerInfo(eqpId, "ready-to-next");
		SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).printCounterInfo();
		
		return retVals;
	}
	
	public String[] checkMsgFromSO(String eqpId, String appNodeName, String applicationName, String procInstanceId) {		
    	String[] retVals =null;
    	
		/*
		 * 1. SWWorkerInfo 상태 체크: 
		 * 2. 요청 메시지 항목이 무엇인지 확인하고 작업을 수행한다.
		 * 3. 작업이 완료되었다면 결과 메시지를 만든다.
		 */
        if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId) == null) {
           
           retVals = new bwReturnValues().retVal1("error-not-found-swworker", String.format("[%s] 장비의 SWWorkerInfo 정보가 없습니다. 체크 알람 메시지 생성 필요", eqpId));
           
        } else if(SWWorkerInfoManager.getInstance().getSWWorkerInfo(eqpId).getSORequest().equalsIgnoreCase("stop-request")) {
        
    		String req = new SORequestCommand().make("release-swworker", eqpId, appNodeName, applicationName, procInstanceId);
    		retVals = new bwReturnValues().retVal1("succeed-release-worker", req);
        
        } else {
        
           retVals[0] = "error-no-match-command";
        }
		
		logger.log(Level.INFO,String.format("[%s][%s] checkMsgFromSO 후 작업을 마무리 하겠습니다.", eqpId, procInstanceId));
		return retVals;
	}
	
	/*
 	* Activate Startup 관련 메시지
 	*/
	public String[] onStartUpEvent(String appNodeName, String applicationName, String procInstanceId ) {
		String retVal = String.format("[%s][%s][%s] 초기화 작업을 수행해야 합니다.", appNodeName, applicationName, procInstanceId);
		/*
		 * 로그 초기화를 수행합니다. - 카프카 핸들러를 기본으로 합니다. - EMS를 로깅서버로 사용합니다.
		 */
		logger.log(Level.INFO, retVal);
		return new bwReturnValues().retVal1("succeed-initialize-in-startup", retVal);
	}
	
	public String[] initLogServerInfo(String appNodeName, String applicationName, String emsLogServerUrl, String emsLogTopicName) {
		/*
		 * LogManager EMS 서버 설정 방법 = 가장 먼저해야 하는 설정 작업
		 * LogManager.getInstance().initializeEMSHandler(applicationName, "tcp://192.168.232.142:7222", "topic.neesp.log");
		 */
		String retVal = String.format("[%s][%s][%s][%s] LogManager 초기화 작업이 완료하였습니다..", appNodeName, applicationName, emsLogServerUrl, emsLogTopicName);
		System.err.printf("/// 1. [%s][%s] 초기화 작업을 수행합니다.\n", emsLogServerUrl, emsLogTopicName);
		LogManager.getInstance().initializeEMSHandler(applicationName, emsLogServerUrl, emsLogTopicName);
		System.err.printf("/// 2. [%s][%s] EMS 서버 연결 작업이 완료되었습니다.\n", emsLogServerUrl, emsLogTopicName);
		logger.addHandler(LogManager.getInstance().getEmsLogHandler());
		logger.log(Level.INFO, retVal);
		
		return new bwReturnValues().retVal1("succeed-initialize-logver", retVal);
	}
	
	public String onShutdownEvent(String appNodeName, String applicationName, String procInstanceId ) {
		String retVal = String.format("[%s][%s][%s] 종료전 정리 작업을 수행해야 합니다.", appNodeName, applicationName, procInstanceId);
		logger.log(Level.INFO, retVal);
		
		/*
		 * application 종료 작업을 수행한다. 
		 * - 제일 마지막으로 LogManager Instance에 있는 핸들러들을 정리한다.
		 */
		
		/*
		 * 반드시 삭제해준다.
		 * To-Do shutdown process - 로그 관련 외부 통신 채널을 모두 정리한다.
		 * BaseProxy 관련 로그의 kafka Handler를 정리합니다.		
		 */
		logger.removeHandler(LogManager.getInstance().getEmsLogHandler());
		LogManager.getInstance().deinitliaze();
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
		
		// Thread.currentThread().setContextClassLoader(LoadDynamic.class.getClassLoader());						
		// bedb.registerDataSource("oracle", "oracle", "edward", "system", "tkfkd1104", "jdbc:oracle:thin:@192.168.0.20:1521/XEPDB1",  null, 50);	
		return val;
	}
}


