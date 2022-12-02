package com.skhynix.extern;

import java.util.function.Consumer;
import java.util.function.Function;

public interface BusinessBehavior {
	public String doBusiness(String eventType, String message, Function<Object, Object> metaSource, Consumer<Object> resultConsumer) throws Exception;
//	public <T,R,K> String doBusiness(String eventType, String message, Function<T, R> metaSource, Consumer<K> resultConsumer) throws Exception;
}
