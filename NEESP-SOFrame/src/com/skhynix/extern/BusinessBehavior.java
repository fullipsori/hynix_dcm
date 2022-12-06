package com.skhynix.extern;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Business Logic 정의하는 클래스는 하기 인터페이스를 구현하게 되면 로딩후에 자동으로 호출이 되게 됩니다.
 * 
 * @author fullipsori
 *
 */
public interface BusinessBehavior {
	/**
	 * 
	 * @param eventType
	 * @param message
	 * @param handles 메세지 라우터로 메세지 보낼때는 각 타입(ems/kafka/ftl)에 해당하는 handle 이 필요합니다. 해당 정보는 busines 정의에 따라 향후에 개선이 될 예정입니다.
	 * @param supplier  Logic 진행시 필요한 함수들이 정의된 인터페이스 인스턴스입니다.  해당 supplier 에 mdm/mr 의 함수들이 정의되어 있습니ㅏㄷ.
	 * @param resultConsumer Owner 에서 추가적으로 무언가 동작이 필요한 경우 서로간에 약속된 함수를 정의하여 동작시킬 수  있습니다. 현재는 콘솔출력만 합니다.
	 * @return 로직처리 결과 
	 * @throws Exception
	 */
	public String doBusiness(String eventType, String message, Map<String,String> handles, BusinessSupplier supplier, Consumer<Object> resultConsumer) throws Exception ;
}
