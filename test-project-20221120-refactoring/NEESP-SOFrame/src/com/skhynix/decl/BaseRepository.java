package com.skhynix.decl;

import java.util.Map;

public abstract class BaseRepository implements DynaLoadable {

	public abstract String connectServer(Map<String,String> args);
	public abstract void disconnectServer();
	
	
}
