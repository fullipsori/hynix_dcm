package com.skhynix.extern;

import java.util.List;

public interface Resourceable {
	public boolean create(String handle, String table, Pair<String,? extends Object> key, List<Pair<String,? extends Object>> params);
	public Object retrieve(String handle, String table, Pair<String,? extends Object> key);
	public boolean update(String handle, String table, Pair<String,? extends Object> key, List<Pair<String, ? extends Object>> params);
	public boolean delete(String handle, String table, Pair<String,? extends Object> key);
}
