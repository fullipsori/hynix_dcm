package com.skhynix.messaging;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.hynix.common.StringUtil;
import com.skhynix.decl.BaseConnection;
import com.skhynix.decl.DynaLoadable;
import com.skhynix.decl.Messageable;
import com.skhynix.model.BaseSessModel;
import com.skhynix.model.KafkaSessModel;

public class KafkaMessage extends BaseConnection implements DynaLoadable, Messageable {
	
	public KafkaMessage() {
		// TODO Auto-generated constructor stub
		this.connectableType = "kafka";
	}
	
	public KafkaMessage(String connectableType) {
		this.connectableType = connectableType;
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean sendMessage(String sessionKey, String msg) {
		// TODO Auto-generated method stub
		Optional.ofNullable(clientMap.get(sessionKey)).ifPresent(client -> {
			KafkaSessModel kafkaSessModel = (KafkaSessModel)client;
			if(kafkaSessModel.msgClient != null && !StringUtil.isEmpty(kafkaSessModel.topic)) {
				((KafkaProducer<String, String>)kafkaSessModel.msgClient).send(new ProducerRecord<String, String>(kafkaSessModel.topic, "keyData", msg /*String.format("Test Message: 한글처리가 제대로 되는지 확인한다.")*/));
			}
		});
		return true;
	}

	@Override
	public String receivedMessage(String sessionKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected BaseSessModel initParams(String jsonParams) {
		// TODO Auto-generated method stub
		return StringUtil.jsonToObject(jsonParams, KafkaSessModel.class);
	}

	@Override
	protected boolean connectServer(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(StringUtil.isEmpty(client.serverUrl)) {
			client.serverUrl = "localhost:9092";
			return true;
		}
		return false;
	}

	@Override
	protected void disconnectServer() {
		closeAllSession();
	}
	
	@Override
	protected String getSessionName(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(!KafkaSessModel.class.isInstance(client)) return null;
		KafkaSessModel kafkaSessModel = (KafkaSessModel) client;
		return String.format("%s:%s", Optional.ofNullable(kafkaSessModel.topic).orElse(""), kafkaSessModel.role);
	}

	@Override
	protected BaseSessModel connectSession(BaseSessModel client) {
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
	protected void disconnectSession(BaseSessModel client) {
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
