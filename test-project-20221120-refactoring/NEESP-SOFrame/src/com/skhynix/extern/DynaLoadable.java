package com.skhynix.extern;

public interface DynaLoadable {
	public String getClassDomain();
	public void loadClass();
	public void unloadClass();
}
