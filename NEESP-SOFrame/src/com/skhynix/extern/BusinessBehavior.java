package com.skhynix.extern;

import java.util.function.Consumer;

public interface BusinessBehavior {
	public String doBusiness(String eventType, String message, MetaFunction function, Consumer<Object> resultConsumer) throws Exception;
}
