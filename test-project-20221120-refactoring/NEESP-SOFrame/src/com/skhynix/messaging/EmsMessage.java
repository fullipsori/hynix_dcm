package com.skhynix.messaging;

import java.util.*;
import javax.jms.*;

import com.hynix.common.StringUtil;
import com.skhynix.decl.BaseConnection;
import com.skhynix.decl.DynaLoadable;
import com.skhynix.decl.Messageable;
import com.skhynix.model.BaseSessModel;
import com.skhynix.model.EmsSessModel;

public class EmsMessage extends BaseConnection implements DynaLoadable, Messageable {
	
	public EmsMessage() {
		// TODO Auto-generated constructor stub
		this.connectionInfo = "ems";
	}
	
	public EmsMessage(String connectionInfo) {
		this.connectionInfo = connectionInfo;
	}

	@Override
	public BaseSessModel makeSessModel(String domain, String jsonParams) {
		EmsSessModel model = StringUtil.jsonToObject(jsonParams, EmsSessModel.class);
		if( model != null) model.serverDomain = domain;
		return model;
	}

	@Override
	public String getSessionName(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(!EmsSessModel.class.isInstance(client)) return "";
		EmsSessModel emsSessModel = (EmsSessModel) client;
		return String.format("%s:%s", (emsSessModel.topicName != null) ? emsSessModel.topicName : emsSessModel.queueName, emsSessModel.role);
	}

	@Override
	public BaseSessModel connectSession(BaseSessModel client) {
		if(!EmsSessModel.class.isInstance(client)) return null;
		EmsSessModel emsSessModel = (EmsSessModel) client;
		
		if(serverModel == null || serverModel.serverHandle == null || 
				(StringUtil.isEmpty(emsSessModel.queueName) && StringUtil.isEmpty(emsSessModel.topicName))) return null;

		Connection connection = (Connection)serverModel.serverHandle;
		boolean sendMode = emsSessModel.role.equals("sender")? true : false;
		String queueName = emsSessModel.queueName;
		String topicName = emsSessModel.topicName;
		String deliveryMode = emsSessModel.deliveryMode;
		Integer sessionMode = (emsSessModel.sessionMode.equals("AUTO_ACK")) ? Session.AUTO_ACKNOWLEDGE : Session.CLIENT_ACKNOWLEDGE;

		Session session = null;
		Object msgClient = null;
		Destination destination = null;

		try {
			session = connection.createSession(false, sessionMode);
			if(session != null) {
				if(queueName != null && !queueName.isEmpty()) {
					destination = session.createQueue(queueName);
				}else if(topicName != null && !topicName.isEmpty()) {
					destination = session.createTopic(topicName);
				}
				if(destination != null) {
					msgClient = sendMode ? session.createProducer(destination) : session.createConsumer(destination);
//					msgObject = session.createTextMessage();

					if(msgClient != null) {
						if(MessageProducer.class.isInstance(msgClient)) {
							MessageProducer msgProducer = (MessageProducer)msgClient;
							if(deliveryMode.compareTo("PERSISTENT")==0) {
								msgProducer.setDeliveryMode(javax.jms.DeliveryMode.PERSISTENT);
							} else if(deliveryMode.compareTo("NON_PERSISTENT")==0) {
								msgProducer.setDeliveryMode(javax.jms.DeliveryMode.NON_PERSISTENT);
							} else if(deliveryMode.compareTo("RELIABLE")==0) {
								msgProducer.setDeliveryMode(com.tibco.tibjms.Tibjms.RELIABLE_DELIVERY);
							}
							msgProducer.setDisableMessageID(true);
							msgProducer.setDisableMessageTimestamp(true);
						}else if(MessageConsumer.class.isInstance(msgClient)) {
							MessageConsumer msgConsumer = (MessageConsumer)msgClient;
							// no action
						}

						emsSessModel.session = session;
						emsSessModel.msgClient = msgClient;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return emsSessModel;
	}

	@Override
	public void disconnectSession(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(!EmsSessModel.class.isInstance(client)) return;
		EmsSessModel emsSessModel = (EmsSessModel) client;
		
		if(emsSessModel.msgClient != null) {
			try {
				if(MessageProducer.class.isInstance(emsSessModel.msgClient)) ((MessageProducer)emsSessModel.msgClient).close();
				else if(MessageConsumer.class.isInstance(emsSessModel.msgClient)) ((MessageConsumer)emsSessModel.msgClient).close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			emsSessModel.msgClient = null;
		}
		if(emsSessModel.session != null) {
			try {
				((Session)emsSessModel.session).close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			emsSessModel.session = null;
		}
	}

	@Override
	public boolean connectServer(BaseSessModel client) {
		if(StringUtil.isEmpty(client.serverUrl)) {
			client.serverUrl = "localhost:7222";
		}
		String serverUrl = client.serverUrl;
		String username = client.username;
		String password = client.password;

		try {
			ConnectionFactory factory = new com.tibco.tibjms.TibjmsConnectionFactory(serverUrl);
			Connection connection = factory.createConnection(username, password);
			connection.start();
			serverModel.serverHandle = connection;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public void disconnectServer() {
		// TODO Auto-generated method stub
		
		closeAllSession();
		
		if(serverModel.serverHandle != null) {
			try {
				Connection conn = (Connection) serverModel.serverHandle;
				conn.stop();
				conn.close();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			serverModel.serverHandle = null;
		}
		
	}

	public String getMessageType() {
		// TODO Auto-generated method stub
		return "ems";
	}


	@Override
	public boolean sendMessage(String sessionKey, String msg) {
		Optional.ofNullable(clientMap.get(sessionKey)).ifPresent(client -> {
			System.out.println("send key:" +sessionKey);
			EmsSessModel emsSessModel = (EmsSessModel) client;
			if(emsSessModel.session != null && emsSessModel.msgClient != null) {
				Session session = (Session)emsSessModel.session;
				try {
					TextMessage message = session.createTextMessage();
					message.setText(msg);
					MessageProducer msgProducer = (MessageProducer)emsSessModel.msgClient;
					msgProducer.send(message);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		return true;
	}

	@Override
	public String receiveMessage(String sessionKey) {
		// TODO Auto-generated method stub
		return Optional.ofNullable(clientMap.get(sessionKey)).map(client -> {
			EmsSessModel emsSessModel = (EmsSessModel) client;
			if(emsSessModel.msgClient != null) {
				try {
					TextMessage message = (TextMessage) ((MessageConsumer)emsSessModel.msgClient).receive();
					if(message != null) return message.getText();
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			return "";
		}).orElse("");
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
	}

}