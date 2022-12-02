package com.skhynix.extern;

import com.skhynix.model.BaseSessModel;

public interface Sessionable {

	public BaseSessModel makeSessModel(String domain, String jsonParams);

	public boolean connectServer(BaseSessModel client);
	public void disconnectServer();

	public String getSessionName(BaseSessModel client);
	public BaseSessModel connectSession(BaseSessModel client);
	public void disconnectSession(BaseSessModel client);

	public String openSession(String domain, String serverUrl, String jsonParams); 
	public boolean closeSession(String sessionKey);
	public void closeAllSession();
}
