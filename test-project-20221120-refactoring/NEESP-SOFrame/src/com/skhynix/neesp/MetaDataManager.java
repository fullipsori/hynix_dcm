package com.skhynix.neesp;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.skhynix.neesp.log.LogManager;

public class MetaDataManager {
	
	// private static final LogManager logger = LogManager.getInstance();	
	private static Logger logger = Logger.getLogger(BaseProxy.class.getName());
	
	public MetaDataManager() {
		System.out.println("/// MetaDataManager called");
	}
	
	public String onStartUpEvent(String appNodeName, String applicationName, String procInstanceId ) {
		String retVal = String.format("[%s][%s][%s] 초기화 작업을 수행해야 합니다.", appNodeName, applicationName, procInstanceId);
		LogManager.getInstance().initialize(appNodeName);		
		logger.addHandler(LogManager.getInstance().getKafkaHandler(appNodeName,applicationName, ""));
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
	
	public String[] allocatSWN_forNEWEQP(String eqpID) {
		String[] retVals = {"",""};
		retVals[0] = "queue.so.sw-node-1.ctl"; // 장비 제어용 토픽 토픽 진행
		retVals[1] = String.format("{\"eqp_id\":\"%s\",\"swnode_id\":\"%s\",\"msg\":\"%s\"}", eqpID, "swnode_01","새롭게 장비가 추가되었습니다. Worker 생성 필요.");
		logger.log(Level.INFO,String.format("[%s] 장비 할당 요청 이벤트 메시지 발신 [%s] ", eqpID, retVals[1]));
		return retVals;
	}
	
	public String createEQPEvent(int EQPseqno) {
		return String.format("[%d][%s] 장비 메시지를 전달하였습니다.", EQPseqno, "createEQPEvent");
	}
	
	public String initSWWorker(String EQPId, String bwappid) {
		logger.log(Level.INFO,String.format("[%s][%s] 장비를 위한 SW-Worker 초기화 작업을 완료하였습니다.", EQPId, bwappid));
		return String.format("[%s][%s] 장비를 위한 SW-Worker 초기화 작업을 완료하였습니다.", EQPId, bwappid);
	}
	
	public String updateSWWorkerInfo(String messageBody) {
		logger.log(Level.INFO, String.format("[%s]",messageBody));
		return String.format("[%s]",messageBody);
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
