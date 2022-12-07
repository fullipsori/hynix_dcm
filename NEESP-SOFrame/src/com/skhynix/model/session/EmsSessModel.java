package com.skhynix.model.session;

import java.io.Serializable;

import javax.jms.Session;

import com.tibco.tibjms.Tibjms;

public class EmsSessModel extends BaseSessModel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static enum SESS_MODE {
		AUTO("AUTO", Session.AUTO_ACKNOWLEDGE),
		CLIENT("CLIENT", Session.CLIENT_ACKNOWLEDGE),
		DUPS_OK("DUPS_OK", Session.DUPS_OK_ACKNOWLEDGE),
		EXPLICIT_CLIENT("EXPLICIT_CLIENT", Tibjms.EXPLICIT_CLIENT_ACKNOWLEDGE),
		EXPLICIT_CLIENT_DUPS_OK("EXPLICIT_CLIENT_DUPS_OK", Tibjms.EXPLICIT_CLIENT_DUPS_OK_ACKNOWLEDGE),
		NO("NO", Tibjms.NO_ACKNOWLEDGE);
		
		public final String modeType;
		public final int modeValue; 

		private SESS_MODE(String modeType, int modeValue) {
			// TODO Auto-generated constructor stub
			this.modeType = modeType;
			this.modeValue = modeValue;
		}
		
    	public static SESS_MODE getSessMode(String modeType) {
    		if(modeType.equals(AUTO.modeType)) return AUTO;
    		if(modeType.equals(CLIENT.modeType)) return CLIENT;
    		if(modeType.equals(DUPS_OK.modeType)) return DUPS_OK;
    		if(modeType.equals(EXPLICIT_CLIENT.modeType)) return EXPLICIT_CLIENT;
    		if(modeType.equals(EXPLICIT_CLIENT_DUPS_OK.modeType)) return EXPLICIT_CLIENT_DUPS_OK;
    		if(modeType.equals(NO.modeType)) return NO;
    		return AUTO;
    	}
	}
	
	public static enum DELIVERY_MODE {
		PERSISTENT("PERSISTENT", javax.jms.DeliveryMode.PERSISTENT),
		NON_PERSISTENT("NON_PERSISTENT", javax.jms.DeliveryMode.NON_PERSISTENT),
		RELIABLE("RELIABLE", Tibjms.RELIABLE_DELIVERY);
		
		public final String modeType;
		public final int modeValue; 
		private DELIVERY_MODE(String modeType, int modeValue) {
			this.modeType = modeType;
			this.modeValue = modeValue;
		}
		
		public static DELIVERY_MODE getDeliveryMode(String modeType) {
    		if(modeType.equals(PERSISTENT.modeType)) return PERSISTENT;
    		if(modeType.equals(NON_PERSISTENT.modeType)) return NON_PERSISTENT;
    		if(modeType.equals(RELIABLE.modeType)) return RELIABLE;
    		return NON_PERSISTENT;
		}
	}
	
	public String queueName = null;
	public String topicName = null;
	public String deliveryMode = DELIVERY_MODE.NON_PERSISTENT.modeType;
	public String sessionMode = SESS_MODE.AUTO.modeType;
	public String selector = "";
	

	public transient Object session = null;
	public transient Object message = null;
	public transient Object msgClient = null;
	public transient Object destination = null;
	public transient Object replyto = null;

	public SESS_MODE getSessMode() {
		return SESS_MODE.getSessMode(sessionMode);
	}
	
	public DELIVERY_MODE getDeliveryMode() {
		return DELIVERY_MODE.getDeliveryMode(deliveryMode);
	}
}
