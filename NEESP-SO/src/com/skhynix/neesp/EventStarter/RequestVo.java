package com.skhynix.neesp.EventStarter;

@SuppressWarnings("serial")
public class RequestVo implements java.io.Serializable {
	private String eqpId = null;

	public RequestVo() {
	}

	public RequestVo(String eqpId) {
		this.eqpId = eqpId;
	}

	public String getEqpId() {
		return eqpId;
	}

	public void setEqpId(String eqpId) {
		this.eqpId = eqpId;
	}
	
}
