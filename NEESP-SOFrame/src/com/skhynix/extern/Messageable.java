package com.skhynix.extern;

import java.util.Map;

import com.skhynix.model.message.MessageModel;

/**
 * 외부에서 MessageManager 에게 Message 를 주고 받는 요청을 하는 경우에 MessageManager 는 등록된
 * 클라이언트에게 하기 인터페이스에 정의된 함수들을 호출하게 됩니다.
 * 그럼으로 메세지 전송이 필요한경우 하기 인터페이스를 구현하고 MessageManager 에게 자신을 join 하게 되면 MessageManager 가 관리를 하게 됩니다.
 * @author fullipsori
 *
 */
public interface Messageable {
	public boolean sendMessage(String handle, String msg, Map<String,String> properties);
	public MessageModel receiveMessage(String handle);
	public void confirmMessage(String handle);
	public MessageModel sendAndReceive(String handle, String msg, Map<String,String> properties, String replyQueue, String selector);
}
