package com.skhynix.repository;

public class DBRepository {

	private static final DBRepository instance = new DBRepository();
	
	public DBRepository() {}
	public static DBRepository getInstance() {
		return instance;
	}
}
