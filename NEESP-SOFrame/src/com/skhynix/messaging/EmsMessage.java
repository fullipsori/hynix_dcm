package com.skhynix.messaging;

import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.skhynix.base.BaseConnection;
import com.skhynix.common.StringUtil;
import com.skhynix.extern.Messageable;
import com.skhynix.model.message.BaseMsgModel;
import com.skhynix.model.session.BaseSessModel;
import com.skhynix.model.session.EmsSessModel;
import com.skhynix.model.session.EmsSessModel.SESS_MODE;

public class EmsMessage extends BaseConnection implements Messageable {
	
	private final String defaultServerUrl = "localhost:7222";
	
	public EmsMessage(String connectionInfo, String serverUrl) {
		this.connectionInfo = String.format("%s%s%s", connectionInfo, BaseSessModel.defaultDelimiter, (StringUtil.isEmpty(serverUrl)) ? defaultServerUrl : serverUrl);
	}
	

	@Override
	public String getDefaultServerUrl() {
		// TODO Auto-generated method stub
		return defaultServerUrl;
	}

	@Override
	public BaseSessModel makeSessModel(String domain, String jsonParams) {
		return StringUtil.jsonToObject(jsonParams, EmsSessModel.class);
	}

	@Override
	public String getSessionName(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(!EmsSessModel.class.isInstance(client)) return "";
		EmsSessModel emsSessModel = (EmsSessModel) client;
		return String.format("%s%s%s%s%s", (emsSessModel.topicName != null) ? emsSessModel.topicName : emsSessModel.queueName, 
				BaseSessModel.defaultDelimiter,
				StringUtil.isEmpty(emsSessModel.selector)? "" : emsSessModel.selector, 
				BaseSessModel.defaultDelimiter,
				emsSessModel.role);
	}
	
	@Override
	public String tokenizeSessionName(String prefixHandle) {
		// TODO Auto-generated method stub
		String[] tokens = prefixHandle.split(BaseSessModel.defaultDelimiter);
		/* prefix,%s,%s,%s*/
		int size = tokens.length;
		return String.format("%s%s%s%s%s", tokens[size-3], BaseSessModel.defaultDelimiter, tokens[size-2], BaseSessModel.defaultDelimiter, tokens[size-1]);
	}

	@Override
	public BaseSessModel connectSession(BaseSessModel client) {
		if(!EmsSessModel.class.isInstance(client)) return null;
		EmsSessModel emsSessModel = (EmsSessModel) client;
		
		if(serverModel == null || serverModel.serverConnection == null || 
				(StringUtil.isEmpty(emsSessModel.queueName) && StringUtil.isEmpty(emsSessModel.topicName))) return null;

		Connection connection = (Connection)serverModel.serverConnection;
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
					emsSessModel.destination = destination;
					msgClient = sendMode ? session.createProducer(destination) 
							: (StringUtil.isEmpty(emsSessModel.selector)? session.createConsumer(destination) : session.createConsumer(destination, emsSessModel.selector));
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
			serverModel.serverConnection = connection;
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
		
		if(serverModel.serverConnection != null) {
			try {
				Connection conn = (Connection) serverModel.serverConnection;
				conn.stop();
				conn.close();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			serverModel.serverConnection = null;
		}
		
	}

	public String getMessageType() {
		// TODO Auto-generated method stub
		return "ems";
	}


	@Override
	public boolean sendMessage(String handle, String msg, Map<String,String> properties) {
		Object client = sessionMap.get(handle);
		if(client != null && EmsSessModel.class.isInstance(client)) {
			EmsSessModel emsSessModel = (EmsSessModel) client;
			if(emsSessModel.session != null && emsSessModel.msgClient != null) {
				Session session = (Session)emsSessModel.session;
				try {
					TextMessage message = session.createTextMessage();
					message.setText(msg);
					properties.entrySet().forEach(entry -> {
						try {
							message.setStringProperty(entry.getKey(), entry.getValue());
						} catch (JMSException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
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

	@SuppressWarnings("unchecked")
	@Override
	public BaseMsgModel receiveMessage(String handle, long waitTimeInMillis) throws Exception {
		// TODO Auto-generated method stub
		Object client = sessionMap.get(handle);
		if(client != null && EmsSessModel.class.isInstance(client)) {
			EmsSessModel emsSessModel = (EmsSessModel) client;
			if(emsSessModel.session != null && emsSessModel.msgClient != null) {
				Message message = (TextMessage) ((MessageConsumer)emsSessModel.msgClient).receive(waitTimeInMillis);
				if(message != null) {
					BaseMsgModel msgModel = new BaseMsgModel();
					msgModel.received.put("message", message.getBody(String.class));
					if(emsSessModel.getSessMode() == SESS_MODE.CLIENT || emsSessModel.getSessMode() == SESS_MODE.EXPLICIT_CLIENT || emsSessModel.getSessMode() == SESS_MODE.EXPLICIT_CLIENT_DUPS_OK)
						emsSessModel.message = message;

					Enumeration<String> enumeration = message.getPropertyNames();
					while(enumeration.hasMoreElements()) {
						String name = (String)enumeration.nextElement();
						msgModel.received.put(name, message.getStringProperty(name));
					}
					return msgModel;
				}
			}
		}
		return null;
	}
	
	/* fullip: refactoring */
	@Override
	public BaseMsgModel sendAndReceive(String handle, String msg, Map<String,String> properties, String replyQueue, String selector, long waitTimeInMillis) {
		// TODO Auto-generated method stub
		Object client = sessionMap.get(handle);
		if(client == null || !EmsSessModel.class.isInstance(client)) return null;
		EmsSessModel emsSendModel = (EmsSessModel) client;
		
		EmsSessModel recvModel = new EmsSessModel();
		recvModel.queueName = replyQueue;
		recvModel.serverDomain = emsSendModel.serverDomain;
		recvModel.role = "receiver";
		recvModel.selector = selector;

		String recvSessionName = getSessionName(recvModel);
		String receivePrefixKey= String.format("%s%s%s", recvModel.serverDomain, BaseSessModel.defaultDelimiter, recvSessionName);
		
		Optional<Entry<String,BaseSessModel>> elem = sessionMap.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith(receivePrefixKey)).findAny();
		String recvOpenHandle = "";
		if(elem.isEmpty()) {
			recvOpenHandle = openSession(recvModel.serverDomain, null, StringUtil.objectToJson(recvModel));
			if(StringUtil.isEmpty(recvOpenHandle)) {
				return null;
			}else {
				recvModel = (EmsSessModel)sessionMap.get(recvOpenHandle);
			}
		}else {
			recvModel = (EmsSessModel)elem.get().getValue();
		}

		if(recvModel != null) {
			if(emsSendModel.session != null && emsSendModel.msgClient != null && recvModel.destination != null) {
				Session session = (Session)emsSendModel.session;
				try {
					TextMessage message = session.createTextMessage();
					message.setJMSReplyTo((Destination)recvModel.destination);
					message.setText(msg);
					properties.entrySet().forEach(entry -> {
						try {
							message.setStringProperty(entry.getKey(), entry.getValue());
						} catch (JMSException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
					MessageProducer msgProducer = (MessageProducer)emsSendModel.msgClient;
					msgProducer.send(message);
					return receiveMessage(recvOpenHandle, waitTimeInMillis);
				}catch(Exception e) {
					e.printStackTrace();
				}finally {
					if(elem.isEmpty() && StringUtil.isNotEmpty(recvOpenHandle)) {
						closeSession(recvOpenHandle);
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public void confirmMessage(String handle) {
		// TODO Auto-generated method stub
		Object client = sessionMap.get(handle);
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
	
}