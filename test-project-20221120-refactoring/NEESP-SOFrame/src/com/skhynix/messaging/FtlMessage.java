package com.skhynix.messaging;

import java.time.Instant;
import java.util.Optional;

import com.hynix.common.StringUtil;
import com.skhynix.decl.BaseConnection;
import com.skhynix.decl.DynaLoadable;
import com.skhynix.decl.Messageable;
import com.skhynix.model.BaseSessModel;
import com.skhynix.model.FtlSessModel;
import com.skhynix.neesp.log.LogManager;
import com.tibco.ftl.FTL;
import com.tibco.ftl.FTLException;
import com.tibco.ftl.Message;
import com.tibco.ftl.Publisher;
import com.tibco.ftl.Realm;
import com.tibco.ftl.TibProperties;

public class FtlMessage extends BaseConnection implements DynaLoadable, Messageable {
	private final LogManager logger = LogManager.getInstance();

	public FtlMessage() {
		// TODO Auto-generated constructor stub
		this.connectionInfo = "ftl";
	}
	
	public FtlMessage(String connectionInfo) {
		this.connectionInfo = connectionInfo;
	}

	public boolean sendMessage(String sessionKey, String msg) {
		// TODO Auto-generated method stub
		Optional.ofNullable(clientMap.get(sessionKey)).ifPresent(client -> {
			FtlSessModel ftlSessModel = (FtlSessModel) client;
			if(ftlSessModel.publisher != null && ftlSessModel.msgObject != null) {
				Message msgObject = (Message)ftlSessModel.msgObject;
				Publisher publisher = (Publisher)ftlSessModel.publisher;
				try {
					msgObject.setString("type", "hello");
					msgObject.setString("message", msg);
					publisher.send(msgObject);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		return true;
	}

	@Override
	public String receiveMessage(String sessionKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseSessModel makeSessModel(String domain, String jsonParams) {
		// TODO Auto-generated method stub
		FtlSessModel model = StringUtil.jsonToObject(jsonParams, FtlSessModel.class);
		if( model != null) model.serverDomain = domain;
		return model;
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
			ftlSessModel.serverUrl = "localhost:8585";
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
			serverModel.serverHandle = FTL.connectToRealmServer(serverUrl, applicationName, props);

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
			if(serverModel.serverHandle != null) {
				((Realm)serverModel.serverHandle).close();
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
	public BaseSessModel connectSession(BaseSessModel client) {
		// TODO Auto-generated method stub
		if(!FtlSessModel.class.isInstance(client)) return null; 
		FtlSessModel ftlSessModel = (FtlSessModel) client;

		String endpointName    = (StringUtil.isEmpty(ftlSessModel.endpointName))? "default" : ftlSessModel.endpointName;
		if(serverModel.serverHandle != null) {
			Realm realm = (Realm) serverModel.serverHandle;
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

}
