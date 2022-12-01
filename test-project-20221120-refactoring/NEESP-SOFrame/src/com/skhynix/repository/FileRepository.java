package com.skhynix.repository;

import java.nio.file.Files;
import java.nio.file.Paths;

public class FileRepository {
	private static final FileRepository instance = new FileRepository();
	
	public FileRepository() {}
	public static FileRepository getInstance() {
		return instance;
	}
	
	public String getData(String filePath) {
		return "";	
	}
}
