package com.skhynix.extern;

public interface MetaFunction {
	Object requestMeta(String sourceType, String table, Object params);
	Object controlMeta(String sourceType, Object data, Object params);
	boolean storeMeta(String sourceType, Object data, Object params);
}
