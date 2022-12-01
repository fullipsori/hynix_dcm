package com.skhynix.model;

import java.io.Serializable;



public class EmsSessModel extends BaseSessModel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String queueName = null;
	public String topicName = null;
	public String deliveryMode = "NON_PERSISTENT";
	public String sessionMode = "AUTO_ACK";
		
	public transient Object session = null;
	public transient Object msgClient = null;
	public transient Object destination = null;
	public transient Object replyto = null;

}
