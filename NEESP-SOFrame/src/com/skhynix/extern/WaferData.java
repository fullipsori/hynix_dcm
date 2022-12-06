package com.skhynix.extern;

import java.io.Serializable;

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

