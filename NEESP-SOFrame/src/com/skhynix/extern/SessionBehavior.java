package com.skhynix.extern;

import com.skhynix.model.session.BaseSessModel;

/**
 * Session 연결이 필요한 클래스들은 다음의 인터페이스를 구현하고, BaseConnection 추상클래스를 상속받게 되면
 * 서버연결과 세션관리가 모두 추상화 됩니다. 
 * 그리고 실제동작은 session handle 이 있으면 모든 동작들을 수행할 수 있게 됩니다.
 * 현재는 ems/kafka/ftl/as 등이 이의 형태로 구현되어 있습니다. 
 * @author fullipsori
 *
 */
public interface SessionBehavior {

	public BaseSessModel makeSessModel(String domain, String jsonParams);

	public boolean connectServer(BaseSessModel client);
	public void disconnectServer();

	public String getSessionName(BaseSessModel client);
	public BaseSessModel connectSession(BaseSessModel client);
	public void disconnectSession(BaseSessModel client);

	public String openSession(String domain, String serverUrl, String jsonParams); 
	public boolean closeSession(String handle);
	public void closeAllSession();
}
