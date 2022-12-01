package com.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.lang.reflect.*;

public class LoadDynamic {
	private Map<String, URLClassLoader> loaderMap = new HashMap<String, URLClassLoader>();
	private Map<String, Object> instanceMap = new HashMap<>();
	
	public LoadDynamic() {
		WaperData.initialize(null);
	}
	
	public void loadJar(String className, String jarFilePath) {

		File jarFile = new File(jarFilePath);
		
		if(loaderMap.containsKey(className)) {
			unload(className);
		}

		try {
			URL classURL = new URL("jar:" + jarFile.toURI().toURL() + "!/");
			URLClassLoader classLoader = new URLClassLoader(new URL [] {classURL});
			loaderMap.put(className, classLoader);
			System.out.println("load success:" + className + " path:" + jarFilePath);
			
			try {
				Class<?> clazz = classLoader.loadClass(className);
				if(clazz != null) {
					Object object = clazz.getDeclaredConstructor().newInstance();

					/** Function Interface type 으로 초기화 한다. **/
					Method	method = object.getClass().getMethod("initialize", Function.class);
					if(method == null) {
						System.out.println("not found");
					}
					method.invoke(object, WaperData.getInstance()); // static method doesn't have an instance
					instanceMap.put(className, object);
					System.out.println(className + " is initialized!");
				}else {
					System.out.println(className + " is not initialized!");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(className + " is not initialized!");
				return ;
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			return;
		}
	}

	private boolean unload(String className) {
		// TODO Auto-generated method stub
		// key 와 연관된 class Object 를 삭제한다.
		Object object = instanceMap.remove(className);
		if(object != null) {
			try {
				Method method = object.getClass().getMethod("deInitialize");
				method.invoke(object);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		URLClassLoader loader = loaderMap.get(className);
		if(loader == null) return true;
		try {
			loader.close();
			return true;
		}catch (IOException e) {
			e.printStackTrace();
			return false;
		}finally {
			loaderMap.remove(className);
			System.out.println("unload success:" + className);
		}
	}
	
	public Object getInstance(String className) {
		Object object = instanceMap.get(className);
		if(object != null) return object;
		return null;
	}

	public String invokeMethod(String className, String methodName) {
		try {
			Object object = getInstance(className);
			Method publicMethod = object.getClass().getDeclaredMethod(methodName);
			String result = (String)publicMethod.invoke(object);
			return result;
		}catch(Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public String convertData(String className, String methodName, String tableName, long key, String data) {
		try {
			Object object = getInstance(className);
			if(object == null) {
				System.out.println("object is null");
				return "";
			}
			Method publicMethod = object.getClass().getMethod(methodName, String.class, long.class, String.class);
			String result = (String)publicMethod.invoke(object, tableName, key, data);
			return result;
		}catch(Exception e) {
			e.printStackTrace();
			return "";
		}
	}

}
