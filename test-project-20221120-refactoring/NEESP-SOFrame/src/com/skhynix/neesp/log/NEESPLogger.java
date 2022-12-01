package com.skhynix.neesp.log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NEESPLogger {
	
	private String eqpId = "";
	private String swnodeId = "";
	
	private long trxStartTime = 0;
	private long trxLastUpdateTime = 0;
	private long trxTotalElapsedTime = 0;
	private long trxSeq = 0;
	private String trxId = "";
	private String gtxnId = "";
	private String messageId = "";
	
	private Logger eqpLogger = null;
	private Logger sysLogger = null;
	private Handler eqpLogHandler = null;
	
	private boolean trxOnOff = true;
	private boolean sysOnOff = true;	
	private boolean rtOnOff = true;	// 로그시 바로 전송할지 LogManager의 큐에 넣어서 비동기 방식으로로 보낼 것인지 결정한다. - 디폴트 값: 실시간 	 
	
	private long sysStartTime = 0;
	private long sysLastUpdateTime = 0;
	private long sysTotalElapsedTime = 0;
	private String commonLogHeader = "";
	
	
	public NEESPLogger (String eqpId, String swnodeId, Logger sysLogger) {
		System.out.printf("/// NEESPLogger: [%s][%s]\n", eqpId, swnodeId);
		this.eqpId = eqpId;
		this.swnodeId = swnodeId;		
		this.sysLogger = sysLogger;		
		this.sysStartTime = System.currentTimeMillis();
		this.sysLastUpdateTime = this.sysStartTime;		
		this.commonLogHeader = String.format("[%s][%s]", swnodeId, eqpId);
		// 장비명을 가지고 만들어준다.
		eqpLogger = Logger.getLogger(eqpId);
	}
	
	public boolean isTrxlogOn () { return trxOnOff; }
	public boolean isSyslogOn () { return sysOnOff; }
	public boolean isSyncLog() { return rtOnOff; }
	public void trxlogOn() { trxOnOff = true; }
	public void syslogOn() { sysOnOff = true; }
	public void trxlogOff() { trxOnOff = false; }
	public void syslogOff() { sysOnOff = false; }
	public void realtimeLogOn() { rtOnOff = true; }
	public void realtimeLogOff() { rtOnOff = false; }
	
	public long getTrxTotalElapsedTime(String gtxnId) {
		if(this.gtxnId.equalsIgnoreCase(gtxnId)) return this.trxTotalElapsedTime;
		else return -1;
	}
	
	/*
	 * Message Id와 gtxnId가 같을 수 있다. - 혹은 Request/ Reply를 위하여 미들웨어적으로 별도의 ID를 부여해서 보내줄 수 있다.
	 */
	public String getGtxnId() { return this.gtxnId; }
	public String getMessageId() { return this.messageId; }
	public long getTrxTotalElapsedTime() { return this.trxTotalElapsedTime; }
	
	public void setSysLogger(Logger globalLogger) {
		this.sysLogger = globalLogger; // LogManager의 globalLogger를 설정해준다.
	}
	
	public String[] setBasicInfo(String eqpId, String swnodeId) {
		String[] retVals = {"",""};		
		if(this.eqpId.equalsIgnoreCase(eqpId)) {
			this.eqpId = eqpId;
			this.swnodeId = swnodeId;
			this.commonLogHeader = String.format("[%s][%s]", swnodeId, eqpId);
			retVals[0]= "succeed-set-basic-info";
			retVals[1]= "기본값을 정상적으로 설정하였습니다." + commonLogHeader;
		} else {
			retVals[0]= "error-eqp-id-is-not-matched";
			retVals[1]= "logger의 장비 ID가 일치하지 않습니다." + commonLogHeader;
			System.out.println("장비 ID가 일치하지 않습니다.");
		}
		return retVals;
	}
	
	public void setHandler(Handler newHandler) {
		if(this.eqpLogHandler!=null) this.eqpLogHandler.close();
		this.eqpLogHandler = newHandler;
	}
	
	public long calcTrxlogElapsed () {
		long currentTime = System.currentTimeMillis();
		long elapsedTime = currentTime - this.trxLastUpdateTime;
		this.trxLastUpdateTime = currentTime;
		this.trxTotalElapsedTime = currentTime - this.trxStartTime;
		return elapsedTime;
	}
	
	public long calcSyslogElapsed () {
		long currentTime = System.currentTimeMillis();
		long elapsedTime = currentTime - this.sysLastUpdateTime;
		this.sysLastUpdateTime = currentTime;
		this.sysTotalElapsedTime = currentTime - this.sysStartTime;
		return elapsedTime;
	}
	
	/*
	 * EvenType: TCE, ACE, RCE, ECE 등 실제 이벤트 유형
	 */
	public void setTransactionId(String eventType, String gtxnId, String extnId, String messageId) {
		this.gtxnId = gtxnId;
		this.messageId = messageId;
		this.trxId = String.format("[%s|%s|%s|%s]", eventType, gtxnId, extnId, messageId);
	}
	
	/* 
	 * 트랜잭션 메시지 로그 규격
	 *  
	 * 1) commonLogHeader: [Service Worker Node ID][장비 ID]
	 * 2) Transaction ID [eventType|gtxnId|extnId|messageId]
	 * 3) Transaction 내 Seq Number
	 * 4) 실제 로그 메시지
	 * 5) StepElaspedTime: 동일 transaction 이전 단계 부터 현 로그 출력 시점까지의 소요 시간 
	 * 5) TotalElaspedTime: transaction 하나를 완전히 처리하는데까지 소요된 시간
	 *  
	 * 예: [sw-node-1][EQP1][1][TCE][마스터 데이터 정보 조회][2][10] 
	 *    
	 */
	public void logTrxStart(String log, String eventType, String gtxnId, String extnId, String messageId) {
		
		this.trxId = String.format("[%s|%s|%s|%s]", eventType, gtxnId, extnId, messageId);		
		this.trxSeq=1;
		this.trxTotalElapsedTime = 0;
		String logMessage = String.format("%s[START]][%s][%d][%s][%d][%d]", commonLogHeader, trxId, trxSeq++, log, 0, trxTotalElapsedTime);		
		this.trxStartTime = System.currentTimeMillis();
		this.trxLastUpdateTime = System.currentTimeMillis();
		
		if(trxOnOff) {
			if(rtOnOff) eqpLogger.log(Level.INFO, logMessage);
			else LogManager.getInstance().addLog(log);
		}		
		System.out.println(logMessage);
	}
	
	/*
	 * 모든 값이 설정된 것을 기준으로 
	 */
	public void logTrxStart(String log ) {
		
		this.trxSeq=1;
		this.trxTotalElapsedTime = 0;
		String logMessage = String.format("%s[START][%s][%d][%s][%d][%d]", commonLogHeader, trxId, trxSeq++, log, 0, trxTotalElapsedTime);		
		this.trxStartTime = System.currentTimeMillis();
		this.trxLastUpdateTime = System.currentTimeMillis();
		
		if(trxOnOff) {
			if(rtOnOff) eqpLogger.log(Level.INFO, logMessage);
			else LogManager.getInstance().addLog(log);
		}
		System.out.println(logMessage);
	}
	
	public long logTrxProc(String log) {		
		long trxStepElapsedTime = calcTrxlogElapsed();
		String logMessage = String.format("%s[PROC][%s][%d][%s][%d][%d]", commonLogHeader, trxId, trxSeq++, log, trxStepElapsedTime, trxTotalElapsedTime);
		
		if(trxOnOff) {
			if(rtOnOff) eqpLogger.log(Level.INFO, logMessage);
			else LogManager.getInstance().addLog(log);
		}
		
		System.out.println(logMessage);
		
		// 이전 단계부터 소요된 시간을 돌려준다.
		return trxStepElapsedTime;
	}
	
	public long logTrxEnd(String log) {
		long trxStepElapsedTime = calcTrxlogElapsed();
		String logMessage = String.format("%s[END][%s][%d][%s][%d][%d]", commonLogHeader, trxId, trxSeq, log, trxStepElapsedTime, trxTotalElapsedTime);		
		
		if(trxOnOff) {
			if(rtOnOff) eqpLogger.log(Level.INFO, logMessage);
			else LogManager.getInstance().addLog(log);
		}
		
		System.out.println(logMessage);
		// 1개의 트랜잭션 총 소요 시간을 돌려준다.
		return trxStepElapsedTime;
	}
	
	/*
	 * system 프로세싱에서 발생하는 일반적인 로그 출력시 사용 LogManager 에서 설정한 전역 Logger 사용
	 */
	public long syslog(String log) {
		long syslogStepElapsedTime = calcSyslogElapsed();		
		String logMessage = String.format("%s[SYSLOG][%s][%d][%s][%d][%d]", commonLogHeader, trxId, trxSeq, log, syslogStepElapsedTime, sysTotalElapsedTime);
		
		if(sysOnOff) {
			if(rtOnOff) eqpLogger.log(Level.INFO, logMessage);
			else LogManager.getInstance().addLog(log);
		}
		System.out.println(logMessage); 
			
		return syslogStepElapsedTime;
	}
	
}

