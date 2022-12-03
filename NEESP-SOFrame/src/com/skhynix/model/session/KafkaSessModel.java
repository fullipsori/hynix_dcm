package com.skhynix.model.session;

import java.io.Serializable;

public class KafkaSessModel extends BaseSessModel implements Serializable {

	private static final long serialVersionUID = 1L;

	public String acks = "all";
	public int retries = 0;
	public int batch = 16384;
	public int linger = 1;
	public int memory = 33554432;
	public String serializer = "org.apache.kafka.common.serialization.StringSerializer";
	public String topic = null;
	
	public transient Object msgClient;
}
