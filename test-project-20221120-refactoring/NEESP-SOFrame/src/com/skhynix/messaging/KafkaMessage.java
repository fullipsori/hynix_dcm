package com.skhynix.messaging;

import java.util.Optional;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.hynix.base.BaseConnection;
import com.hynix.common.StringUtil;
import com.skhynix.extern.DynaLoadable;
import com.skhynix.extern.Messageable;
import com.skhynix.model.BaseSessModel;
import com.skhynix.model.KafkaSessModel;

public class KafkaMessage extends BaseConnection implements DynaLoadable, Messageable {
	
	private final String defaultServerUrl = "localhost:9092";

	public KafkaMessage() {
		// TODO Auto-generated constructor stub
		this.connectionInfo = "kafka";
	}
	
	public KafkaMessage(String connectionInfo, String serverUrl) {
		this.connectionInfo = String.format("%s:%s", connectionInfo, (StringUtil.isEmpty(serverUrl)) ? defaultServerUrl : serverUrl);
	}
	
	@Override
	public String getDefaultServerUrl() {
		// TODO Auto-generated method stub
		return defaultServerUrl;
	}

	@Override
	public void loadClass() {
		// TODO Auto-generated method stub

	}

	@Override
	public void unloadClass() {
		// TODO Auto-generated method stub
		if(unregister != null) {
			unregister.run();
			unregister = null;
		}
		disconnectServer();
	}
	
	@Override
	public String getClassDomain() {
		// TODO Auto-generated method stub
		return "message:kafka";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean sendMessage(String sessionKey, String msg) {
		Object client = clientMap.get(sessionKey);
		if(client != null  && KafkaSessModel.class.isInstance(client)) {
			KafkaSessModel kafkaSessModel = (KafkaSessModel)client;
			if(kafkaSessModel.msgClient != null && !StringUtil.isEmpty(kafkaSessModel.topic)) {
				((KafkaProducer<String, String>)kafkaSessModel.msgClient).send(new ProducerRecord<String, String>(kafkaSessModel.topic, "keyData", msg /*String.format("Test Message: 한글처리가 제대로 되는지 확인한다.")*/));
				return true;
			}
		}
		return false;
	}

	@Override
	public String receiveMessage(String sessionKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseSessModel makeSessModel(String domain, String jsonParams) {
		// TODO Auto-generated method stub
		KafkaSessModel model = StringUtil.jsonToObject(jsonParams, KafkaSessModel.class);
		if( model != null) model.serverDomain = domain;
		return model;
	}

	@Override
	public boolean connectServer(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(StringUtil.isEmpty(client.serverUrl)) {
			client.serverUrl = defaultServerUrl;
			return true;
		}
		return false;
	}

	@Override
	public void disconnectServer() {
		closeAllSession();
	}
	
	@Override
	public String getSessionName(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(!KafkaSessModel.class.isInstance(client)) return null;
		KafkaSessModel kafkaSessModel = (KafkaSessModel) client;
		return String.format("%s:%s", Optional.ofNullable(kafkaSessModel.topic).orElse(""), kafkaSessModel.role);
	}

	@Override
	public BaseSessModel connectSession(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(!KafkaSessModel.class.isInstance(client)) return null;
		KafkaSessModel kafkaSessModel = (KafkaSessModel) client;

		Properties props = new Properties();

		//Assign localhost id
		props.put("bootstrap.servers", kafkaSessModel.serverUrl);

		//Set acknowledgements for producer requests.      
		props.put("acks", kafkaSessModel.acks);

		//If the request fails, the producer can automatically retry,
		props.put("retries", kafkaSessModel.retries);

		//Specify buffer size in config
		props.put("batch.size", kafkaSessModel.batch);

		//Reduce the no of requests less than 0   
		props.put("linger.ms", kafkaSessModel.linger);

		//The buffer.memory controls the total amount of memory available to the producer for buffering.   
		props.put("buffer.memory", kafkaSessModel.memory);
		//		  Thread.currentThread().setContextClassLoader(null);

		props.put("key.serializer", kafkaSessModel.serializer);		     
		props.put("value.serializer", kafkaSessModel.serializer);

		if(kafkaSessModel.role.equals("sender")) {
			kafkaSessModel.msgClient = new KafkaProducer <String, String>(props);
			
		} else {
//			kafkaClient.msgClient = new KafkaConsumer<String, String>(props);
		}

		return kafkaSessModel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void disconnectSession(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(!KafkaSessModel.class.isInstance(client)) return;
		KafkaSessModel kafkaSessModel = (KafkaSessModel) client;
		
		if(kafkaSessModel.msgClient != null) {
			if(KafkaProducer.class.isInstance(kafkaSessModel.msgClient)) {
				((Producer<String, String>)kafkaSessModel.msgClient).close();
				kafkaSessModel.msgClient = null;
			}
		}
	}

}
