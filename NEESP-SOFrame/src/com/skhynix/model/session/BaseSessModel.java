package com.skhynix.model.session;

import java.io.Serializable;

abstract public class BaseSessModel implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String defaultDelimiter = "-";
	
	public String handle = null;
	public String serverDomain = null;
	public String sessionName = null;
	
	public String serverUrl = null;
	public String username = null;
	public String password = null;
	public String role = "sender";
	
	public transient Object serverConnection = null;
}
