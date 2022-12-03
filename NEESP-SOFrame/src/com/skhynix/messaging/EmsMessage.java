package com.skhynix.messaging;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import com.tibco.tibjms.Tibjms;
import com.skhynix.base.BaseConnection;
import com.skhynix.common.StringUtil;
import com.skhynix.extern.DynaLoadable;
import com.skhynix.extern.Messageable;
import com.skhynix.model.session.BaseSessModel;
import com.skhynix.model.session.EmsSessModel;
import com.skhynix.model.session.EmsSessModel.SESS_MODE;

public class EmsMessage extends BaseConnection implements DynaLoadable, Messageable {
	
	private final String defaultServerUrl = "localhost:7222";
	
	public EmsMessage(String connectionInfo, String serverUrl) {
		this.connectionInfo = String.format("%s:%s", connectionInfo, (StringUtil.isEmpty(serverUrl)) ? defaultServerUrl : serverUrl);
	}

	@Override
	public String getDefaultServerUrl() {
		// TODO Auto-generated method stub
		return defaultServerUrl;
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

		Session session = null;
		Object msgClient = null;
		Destination destination = null;

		try {
			session = connection.createSession(false, emsSessModel.getSessMode().modeValue);
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
							msgProducer.setDeliveryDelay(emsSessModel.getDeliveryMode().modeValue);
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
			client.serverUrl = defaultServerUrl;
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
	public boolean sendMessage(String handle, String msg) {
		Object client = clientMap.get(handle);
		if(client != null && EmsSessModel.class.isInstance(client)) {
			EmsSessModel emsSessModel = (EmsSessModel) client;
			if(emsSessModel.session != null && emsSessModel.msgClient != null) {
				Session session = (Session)emsSessModel.session;
				try {
					TextMessage message = session.createTextMessage();
					message.setText(msg);
					MessageProducer msgProducer = (MessageProducer)emsSessModel.msgClient;
					msgProducer.send(message);
					return true;
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	@Override
	public String receiveMessage(String handle) {
		// TODO Auto-generated method stub
		Object client = clientMap.get(handle);
		if(client != null && EmsSessModel.class.isInstance(client)) {
			EmsSessModel emsSessModel = (EmsSessModel) client;
			if(emsSessModel.session != null && emsSessModel.msgClient != null) {
				try {
					Message message = (TextMessage) ((MessageConsumer)emsSessModel.msgClient).receive();
					if(emsSessModel.getSessMode() == SESS_MODE.CLIENT || emsSessModel.getSessMode() == SESS_MODE.EXPLICIT_CLIENT || emsSessModel.getSessMode() == SESS_MODE.EXPLICIT_CLIENT_DUPS_OK)
						emsSessModel.message = message;
					if(message != null) return ((TextMessage)message).getText();
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}
	
	@Override
	public void confirmMessage(String handle) {
		// TODO Auto-generated method stub
		Object client = clientMap.get(handle);
		if(client != null && EmsSessModel.class.isInstance(client)) {
			EmsSessModel emsSessModel = (EmsSessModel) client;
			if(emsSessModel.message != null && 
					(emsSessModel.getSessMode() == SESS_MODE.CLIENT || emsSessModel.getSessMode() == SESS_MODE.EXPLICIT_CLIENT || emsSessModel.getSessMode() == SESS_MODE.EXPLICIT_CLIENT_DUPS_OK)) {
				try {
					((Message)emsSessModel.message).acknowledge();
					emsSessModel.message = null;
				} catch (JMSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
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