package com.skhynix.manager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import com.hynix.common.Pair;
import com.skhynix.extern.DynaLoadable;
import com.skhynix.neesp.log.LogManager;

import io.reactivex.rxjava3.subjects.PublishSubject;

public class DynaClassManager {
	private static final DynaClassManager dynaClassLoader = new DynaClassManager();

	private LogManager logger = null;
	private final Map<String, URLClassLoader> loaderMap = new HashMap<String, URLClassLoader>();
	private final Map<String, Object> instanceMap = new HashMap<>();	
	
	/* Pair<classCategory, className> */
	public final PublishSubject<Pair<String,String>> loadJarSubject = PublishSubject.create();
	public final PublishSubject<Pair<String,String>> unloadJarSubject = PublishSubject.create();

	public DynaClassManager() {
		logger = LogManager.getInstance();
	}
	
	public static DynaClassManager getInstance() {
		return dynaClassLoader;	
	}
	
	public boolean loadJar(String className, String jarFilePath) {
		
		File jarFile = new File(jarFilePath);
		
		try {
			Thread thread = Thread.currentThread();
			ClassLoader contextLoader = thread.getContextClassLoader();
			URL classURL = new URL("jar:" + jarFile.toURI().toURL() + "!/");
			
			if(loaderMap.containsKey(className)) {
				unloadJar(className);
			}
			
			URLClassLoader urlClassLoader = new URLClassLoader(new URL [] {classURL}, contextLoader);
			loaderMap.put(className, urlClassLoader);

			try {
				Class<?> clazz = urlClassLoader.loadClass(className);
				if(clazz != null) {
					Object classInstance = clazz.getDeclaredConstructor().newInstance();
					instanceMap.put(className, classInstance);
					if(DynaLoadable.class.isInstance(classInstance)) {
						((DynaLoadable) classInstance).loadClass();
						String classDomain = ((DynaLoadable)classInstance).getClassDomain();
						if(classDomain != null) loadJarSubject.onNext(new Pair<>(classDomain, className));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
		}catch(MalformedURLException e) {
			e.printStackTrace();
			return false;
		} 	
		
		return true;
	}

	public boolean unloadJar(String className) {
		Object classInstance = instanceMap.remove(className);
		String classDomain = null;
		if(classInstance != null && DynaLoadable.class.isInstance(classInstance)) {
			((DynaLoadable)classInstance).unloadClass();
			classDomain = ((DynaLoadable)classInstance).getClassDomain();
		}
		
		try {
			URLClassLoader urlClassLoader = loaderMap.remove(className);
			if(urlClassLoader == null) return true;
			urlClassLoader.close();
			return true;
		}catch (IOException e) {
			e.printStackTrace();
			return false;
		}finally {
			if(classDomain != null) unloadJarSubject.onNext(new Pair<>(classDomain, className));
		}
	}

	public Object getClassInstance(String className) {
		return instanceMap.get(className);
	}

}
