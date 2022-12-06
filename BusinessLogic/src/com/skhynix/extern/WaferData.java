package com.skhynix.extern;

import java.io.Serializable;

/**
 * 임시로 정의한 클래스로서 만약 비즈니스 로직과 owner 클래스가 모두 필요한 클래스가 있다면 정의가 필요합니다.
 * 하지만, 서로 주고 받는 타입은 json 으로 진행한다면 owner 모듈에서는 정의할 필요가 없는 클래스 입니다.
 * 일단은 임시로 넣어 놓습니다.
 * @author fullipsori
 *
 */
public class WaferData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String waferId = null;
	public long metadataKey = 0;
	public String sensorData = null;
	public String message = null;
	public String processResult = null;
	
	public WaferData setWaferId(String waferId) {
		this.waferId = waferId;
		return this;
	}

	public WaferData setMetadataKey(long metadataKey) {
		this.metadataKey = metadataKey;
		return this;
	}
	
	public WaferData setSensorData(String sensorData) {
		this.sensorData = sensorData;
		return this;
	}
	
	public WaferData setMessage(String message) {
		this.message = message;
		return this;
	}

	public WaferData setProcessResult(String processResult) {
		this.processResult = processResult;
		return this;
	}
}

