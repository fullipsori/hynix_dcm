package com.skhynix.extern;

import java.util.function.Consumer;
import java.util.function.Function;

public interface BusinessBehavior {
	public String doBusiness(String eventType, String message, MetaFunction function, Consumer<Object> resultConsumer) throws Exception;
}
