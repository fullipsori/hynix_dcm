package com.skhynix.messaging;

import java.time.Instant;
import java.util.Map;

import com.skhynix.base.BaseConnection;
import com.skhynix.common.StringUtil;
import com.skhynix.extern.Messageable;
import com.skhynix.model.message.BaseMsgModel;
import com.skhynix.model.session.BaseSessModel;
import com.skhynix.model.session.FtlSessModel;
import com.skhynix.neesp.log.LogManager;
import com.tibco.ftl.FTL;
import com.tibco.ftl.FTLException;
import com.tibco.ftl.Message;
import com.tibco.ftl.Publisher;
import com.tibco.ftl.Realm;
import com.tibco.ftl.TibProperties;

public class FtlMessage extends BaseConnection implements Messageable {
	private final LogManager logger = LogManager.getInstance();

	private final String defaultServerUrl = "localhost:8585";

	public FtlMessage(String connectionInfo, String serverUrl) {
		this.connectionInfo = String.format("%s%s%s", connectionInfo, BaseSessModel.defaultDelimiter, (StringUtil.isEmpty(serverUrl)) ? defaultServerUrl : serverUrl);
	}

	@Override
	public String getDefaultServerUrl() {
		// TODO Auto-generated method stub
		return defaultServerUrl;
	}

	@Override
	public boolean sendMessage(String handle, String msg, Map<String,String> properties) {
		// TODO Auto-generated method stub
		// fullip : check support of properties
		Object client =  sessionMap.get(handle);
		if(client != null && FtlSessModel.class.isInstance(client)) {
			FtlSessModel ftlSessModel = (FtlSessModel) client;
			if(ftlSessModel.publisher != null && ftlSessModel.msgObject != null) {
				Message msgObject = (Message)ftlSessModel.msgObject;
				Publisher publisher = (Publisher)ftlSessModel.publisher;
				try {
					msgObject.setString("type", "hello");
					msgObject.setString("message", msg);
					/** fullip: add property */
					publisher.send(msgObject);
					return true;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	@Override
	public BaseMsgModel receiveMessage(String handle, long waitTimeInMillis) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public BaseMsgModel sendAndReceive(String handle, String msg, Map<String,String> properties, String replyQueue, String selector, long waittimeInMillis) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void confirmMessage(String handle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BaseSessModel makeSessModel(String domain, String jsonParams) {
		// TODO Auto-generated method stub
		return StringUtil.jsonToObject(jsonParams, FtlSessModel.class);
	}

	@Override
	public boolean connectServer(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(!FtlSessModel.class.isInstance(client)) return false; 
		FtlSessModel ftlSessModel = (FtlSessModel) client;

		if(StringUtil.isEmpty(ftlSessModel.clientName)) {
			String sampleName = "TibFtlSend";
			ftlSessModel.clientName = String.format("%s_%d", sampleName, Instant.now().getEpochSecond());
		}
		
		if(StringUtil.isEmpty(ftlSessModel.serverUrl)) {
			ftlSessModel.serverUrl = defaultServerUrl;
		}

		String clientName =  ftlSessModel.clientName;
		String serverUrl = ftlSessModel.serverUrl;

		String applicationName = (StringUtil.isEmpty(ftlSessModel.applicationName))? "default" : ftlSessModel.applicationName;

		/*
		 * A properties object (TibProperties) allows the client application to set attributes (or properties) for an object
		 * when the object is created. In this case, set the client label property (PROPERTY_STRING_CLIENT_LABEL) for the 
		 * realm connection. 
		 */
		try {
			TibProperties   props = null;
			props = FTL.createProperties();
			props.set(Realm.PROPERTY_STRING_CLIENT_LABEL, clientName);

			/*
			 * Establish a connection to the realm service. Specify a string containing the realm service URL, and 
			 * the application name. The last argument is the properties object, which was created above.

			 * Note the application name (the value contained in applicationName, which is "default"). This is the default application,
			 * provided automatically by the realm service. It could also be specified as null - it mean the same thing,
			 * the default application. 
			 */
			serverModel.serverConnection = FTL.connectToRealmServer(serverUrl, applicationName, props);

			/* 
			 * It is good practice to clean up objects when they are no longer needed. Since the realm properties object is no
			 * longer needed, destroy it now. 
			 */
			props.destroy();
			
		}catch(Exception ex) {
			ex.printStackTrace();
			disconnectServer();
			return false;
		}
		return true;
	}

	@Override
	public void disconnectServer() {
		try {
			if(serverModel.serverConnection != null) {
				((Realm)serverModel.serverConnection).close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String getSessionName(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(!FtlSessModel.class.isInstance(client)) return null; 
		FtlSessModel ftlSessModel = (FtlSessModel) client;
		return ftlSessModel.role;
	}

	@Override
	public String tokenizeSessionName(String prefixHandle) {
		// TODO Auto-generated method stub
		int lastidx = prefixHandle.lastIndexOf(BaseSessModel.defaultDelimiter);
		return prefixHandle.substring(lastidx + 1);
	}

	@Override
	public BaseSessModel connectSession(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(!FtlSessModel.class.isInstance(client)) return null; 
		FtlSessModel ftlSessModel = (FtlSessModel) client;

		String endpointName    = (StringUtil.isEmpty(ftlSessModel.endpointName))? "default" : ftlSessModel.endpointName;
		if(serverModel.serverConnection != null) {
			Realm realm = (Realm) serverModel.serverConnection;
			try {
				ftlSessModel.publisher = realm.createPublisher(endpointName);
				ftlSessModel.msgObject = realm.createMessage("helloworld");
			} catch (FTLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		return ftlSessModel;
	}

	@Override
	public void disconnectSession(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(!FtlSessModel.class.isInstance(client)) return; 
		FtlSessModel ftlSessModel = (FtlSessModel) client;
		
		if(ftlSessModel.publisher != null) {
			try {
				((Publisher)ftlSessModel.publisher).close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			ftlSessModel.publisher = null;
		}
		if(ftlSessModel.msgObject != null) {
			try {
				((Message)ftlSessModel.msgObject).destroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
			ftlSessModel.msgObject = null;
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
