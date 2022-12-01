package com.skhynix.model;

import java.io.Serializable;

abstract public class BaseSessModel implements Serializable {

	private static final long serialVersionUID = 1L;

	public String serverUrl = null;
	public String role = "sender";
	public String username = null;
	public String password = null;
	
	public transient Object serverHandle = null;
}
