package com.skhynix.model;

import java.io.Serializable;

public class FtlSessModel extends BaseSessModel implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String applicationName;
	public String endpointName;
	public String clientName;

	public transient Object publisher = null;
	public transient Object msgObject = null;
}
