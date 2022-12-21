package com.skhynix.manager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.skhynix.common.StringUtil;
import com.skhynix.extern.Pair;

import io.reactivex.rxjava3.subjects.PublishSubject;

public class DynaClassManager {
	private static final DynaClassManager dynaClassLoader = new DynaClassManager();

	private final Map<String, URLClassLoader> loaderMap = new HashMap<String, URLClassLoader>();
	private final Map<String, Class<?>> classMap = new HashMap<>();	
	
	/* Pair<classCategory, className> */
	public final PublishSubject<Pair<String,String>> loadJarSubject = PublishSubject.create();
	public final PublishSubject<Pair<String,String>> unloadJarSubject = PublishSubject.create();

	public DynaClassManager() {
	}
	
	public static DynaClassManager getInstance() {
		return dynaClassLoader;	
	}
	
	public boolean loadJar(String className, String classDomain, String jarFilePath) {
		
		if(StringUtil.isEmpty(className) || StringUtil.isEmpty(classDomain) || StringUtil.isEmpty(jarFilePath)) return false;

		File jarFile = new File(jarFilePath);
		
		try {
//			Thread thread = Thread.currentThread();
//			ClassLoader contextLoader = thread.getContextClassLoader();
			URL classURL = new URL("jar:" + jarFile.toURI().toURL() + "!/");
			
			if(loaderMap.containsKey(className)) {
				unloadJar(className, classDomain);
			}
			
//			URLClassLoader urlClassLoader = new URLClassLoader(new URL [] {classURL}, contextLoader);
			URLClassLoader urlClassLoader = new URLClassLoader(new URL [] {classURL});
			loaderMap.put(className, urlClassLoader);

			try {
				Class<?> clazz = urlClassLoader.loadClass(className);
				classMap.put(className, clazz);
				loadJarSubject.onNext(Pair.of(classDomain, className));
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
		}catch(MalformedURLException e) {
			e.printStackTrace();
			return false;
		} 	
	}

	public boolean unloadJar(String className, String classDomain) {
		if(StringUtil.isEmpty(className) || StringUtil.isEmpty(classDomain)) return false;
		classMap.remove(className);

		try {
			URLClassLoader urlClassLoader = loaderMap.remove(className);
			if(urlClassLoader == null) return true;
			urlClassLoader.close();
			return true;
		}catch (IOException e) {
			e.printStackTrace();
			return false;
		}finally {
			unloadJarSubject.onNext(Pair.of(classDomain, className));
		}
	}

	public Object getClassInstance(String className) {
		return Optional.ofNullable(classMap.get(className))
				.map(clazz -> {
					try {
						Object classInstance = clazz.getDeclaredConstructor().newInstance();
						return classInstance;
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				}).orElse(null);
	}

}
