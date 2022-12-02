package com.skhynix.neesp;

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.*;

import java.util.*;


// AS 처리하기 위하여 사용
import java.util.function.Function;
import java.util.function.Supplier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skhynix.neesp.log.LEVEL;
import com.skhynix.neesp.log.LogManager;

import java.util.logging.*;

import com.tibco.datagrid.Row;
import com.tibco.datagrid.RowSet;

public class LoadDynamic {

//	private static Logger LOGGER = null; 
	
	private static final String logFile = "/Users/fullipsori/temp/dynamicLoading.log";
	private static final LogManager logger = LogManager.getInstance();
	private Map<String, URLClassLoader> loaderMap = new HashMap<String, URLClassLoader>();
	private Map<String, Object> instanceMap = new HashMap<>();	

	public LoadDynamic() {
		String OS = System.getProperty("os.name");
		if(OS.startsWith("Windows")) {			
			LogManager.getInstance().setOutputMode(true,"d:/00_SKHYNIX/test-project/log/lmjava.log");
		} else {
//			logger.setOutputMode(true,"/home/tibco/temp/dynamicLoading.log");
			logger.setOutputMode(true,logFile);
		}
	}
	
	public Map<String, Object> getInstanceMap (String data) {
		logger.debug("인스턴스 맵을 넘겨 줍니다." + data);		
		System.out.println("출력할 데이터: "+data);
		return instanceMap;
	}
	
	public String[] doBusiness(String appNodeName, String eqpID, String eventMessage) {
		String[] retVals = {"",""};
		// 1. event message 파싱 => 
		// 2. event 유형 기반 => event 타입 : 비즈니스 로직 결정 
		// 3. doBusiness 실행 
		// 4. 이벤트 메시지 종류 확인 및 라우팅 경로 결정
		// 5. 메시지 전송
		// 6. Business 로깅
		// 7. 시스템 이벤트 체크 및 처리
		// 8. 다음 메시지 수신 준비
		
		logger.getLogger().log(Level.INFO,String.format("[%s][%s] doBusiness-Processing: Step.1 - Step.8 [%s]", appNodeName, eqpID, eventMessage));		
		
		retVals[0] = "queue.dcm.fdc."+eqpID; // 장비 제어용 토픽 토픽 진행 - 타켓 메시지 - Message Routing
		retVals[1] = String.format("{\"eqp_id\":\"%s\",\"swnode_id\":\"%s\",\"msg\":\"%s\"}", eqpID, appNodeName, eventMessage);		
		return retVals;
	}
	
	public String[] allocatSWN_forNEWEQP(String eqpID) {
		String[] retVals = {"",""};
		retVals[0] = "skh.narf.so.swnode.ctl.t"; // 장비 제어용 토픽 토픽 진행
		retVals[1] = String.format("{\"eqp_id\":\"%s\",\"swnode_id\":\"%s\",\"msg\":\"%s\"}", eqpID, "swnode_01","새롭게 장비가 추가되었습니다. Worker 생성 필요.");		
		return retVals;
	}
	
	public String createEQPEvent(int EQPseqno) {
		return String.format("[%d][%s] 장비 메시지를 전달하였습니다.", EQPseqno, "createEQPEvent");
	}
	
	public String sayHello(String jarFileName, String jarPath) {		
		logger.setLevel(LEVEL.DEBUG);
		logger.getLogger().log(Level.INFO, "test messageg 로그가 어떻게 남는지 확인이 필요하다.");
		
	
		//Thread.currentThread().setContextClassLoader(LoadDynamic.class.getClassLoader());		
		String OS = System.getProperty("os.name");		
		logger.debug("**************** - 새롭게 사용하도록 하겠습니다. LOGGER 사용하기 [OS 버전: "+OS+"]");
		logger.info(String.format("/// LoadDynamic 객체를 최초 생성합니다. - NEConfigInfo 정보를 담을 ArrayList를 같이 만들어 줍니다. ///"));		
		// bedb.registerDataSource("oracle", "oracle", "edward", "system", "tkfkd1104", "jdbc:oracle:thin:@192.168.0.20:1521/XEPDB1",  null, 50);
		logger.debug("**************** - 새롭게 사용하도록 하겠습니다. LOGGER 사용하기 [OS 버전: "+OS+"]");
		
		return "Hello EdwardWord! ["+jarFileName+"]["+jarPath+"]";
	}
	
	public Object getLogger() {
		return logger;
	}
	
	public String loadJar(String jarFileNameArg, String jarPathArg) {
		
		String retVal = "[good]";		
		
		String jarFilePath = jarPathArg+"/"+jarFileNameArg+".jar";		
		File jarFile = new File(jarFilePath);
		String jarPath = jarFile.getParentFile().toString();
		String jarFileName = jarFile.getName();
//		String className = "com.skhynix.narf.test."+jarFileNameArg;
		String className = jarFileNameArg;
		
		if(loaderMap.containsKey(jarFileName)) {
			// unload(jarFileName);
			logger.debug("***************************************");
			logger.debug("* "+jarFileName+"이미 로딩되어 있는 클래스입니다. 사용만 하시면 됩니다. ");			
			logger.debug("**************************************");
		} else {			
			logger.debug("처음 로딩하는 것이군요. "+jarFileName+ " [경로명:"+jarPath+"]");
			try {
				logger.debug("1. creat classURL jar URL String: "+jarFile.toURI().toURL().toString());
				
				URL classURL = new URL("jar:" + jarFile.toURI().toURL() + "!/");
				logger.debug("2. URLClassLoder : "+ jarPath+" : "+ jarFileName);				
				URLClassLoader classLoader = new URLClassLoader(new URL [] {classURL});
				logger.debug("3. put Loader Map : "+ jarPath+" : "+ jarFileName);				
				loaderMap.put(className, classLoader);
				logger.debug("4. Loaded successfully : "+ jarPath+" : "+ jarFileName);
				logger.debug("**************************************");								
				// instanceMap에 Instance를 생성해서 넣어준다.
				logger.getLogger().log(Level.INFO,"5. 인스턴스를 생성합니다. className : "+className+".doBusiness() version2.0");				
				Object obj = newInstance(className);
				
				if(obj != null) {
					instanceMap.put(className, obj);
					logger.debug("6. "+className+"의 인스턴스가 정상적으로 생성되었습니다.");
				} else {
					logger.debug("6. "+className+"생성에 실패하였습니다.");					
					return retVal;
				}
				
			}catch(MalformedURLException e) {
				logger.debug("생성 중 오류가 발생하였습니다. Exception: " + e.toString());
				e.printStackTrace();
				return "[error] "+e.getMessage();
			} 	
		}
		
		return retVal;
	}

	public String unload(String className) {
		// TODO Auto-generated method stub
		URLClassLoader loader = loaderMap.get(className);
		if(loader == null) return "성공";
		try {
			loader.close();			
			loaderMap.remove(className);
			Object value = instanceMap.get(className); // 모든 내용을 정리해준다.	- 상속받은 내용들을 반드시 하기와 같이 없애줘야 한다.		
			return "성공";	
		}catch (IOException e) {
			System.err.println("Exception:" + e.getMessage());
			return "실패";
		}finally {
			loaderMap.remove(className);
			instanceMap.remove(className);
		}
	}
	
	public Object newInstance(String className) {
		
		int i = 1;		
		Object object = null;	
			
		try {			
			URLClassLoader loader = loaderMap.get(className);
			if(loader == null) {
				logger.debug("* URLClassLoader is NULL to loading URL ["+className+"]");
				return null;
			}
			
			Class<?> clazz = loader.loadClass(className);
			object = clazz.getDeclaredConstructor().newInstance();
			
			if(object != null) {
				logger.debug("* find a WAY ["+className+"] => newInstance ["+className+"]을 생성하였습니다.");
				return object;
			} else {
				logger.debug("* object is NULL: newInstance ["+className+"]");
				return null;
			}
		} catch (Exception e) {
			logger.debug("Exception:" + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public String checkAvaiableSWN(String testValue) {
		logger.debug("채널할당 및 세팅 작업 수행: this 기반의 호출이 가능한가!! ["+testValue+"]");
		return "가능한 채널 임의 할당";
	}
	
	public String invokeMethod(String ClassName, String methodName) {
		
//		String className = "com.skhynix.narf.test."+ClassName;
		String className = ClassName;		
		String retVal = "[초기화 상태입니다.]["+className+"]["+methodName+"]";
		
		try {
			// Object my = (Object)this;			
			// Method publicMethod1 = my.getClass().getDeclaredMethod("printTest", String.class);
			// publicMethod1.invoke(my, "test data pass!!!");
			
			// 한번 생성된 것은 또 불러들일 필요가 없다.			
			// String Json 형식으로 넘기고 처리하는 방법을 사용한다.
			Object obj = instanceMap.get(className);
			
			if(obj == null) {				
				logger.debug("2.create NewInstance : "+className+"."+methodName+"()");				
				return "[해당하는 클래스가 존재하지 않습니다. - 오류가 발생하였습니다.]";
			} else {
				logger.debug("* 이미 생성된 인스턴스 입니다. 기존에 있던 객체를 사용합니다!!!!");
			}
			
			Method publicMethod = obj.getClass().getDeclaredMethod(methodName, Object.class, Object.class);
			Method writeDebug = logger.getClass().getDeclaredMethod("debug", String.class);
			
			logger.debug("3.getMethod : "+className+"."+methodName+"()");			
			logger.getLogger().log(Level.INFO,"4.invoke Public Method : "+className+"."+methodName+"(Object, Object)");
			  
			// String retObj  = (String)publicMethod.invoke(obj, logger.getLogger(), this);
			// logger.debug("5.최종 결과값: "+retObj);
			// System.out.println("5.최종 결과값: "+retObj);            
			// Row retObj  = (Row)publicMethod.invoke(obj, logger.getLogger(), this);
			// logger.debug("5.최종 처리 결과값: "+retObj.toString());			
			// Demo retObj  = (Demo)publicMethod.invoke(obj, logger.getLogger(), this);
			
//			객체 직열화 하여 받은 경우
			byte[] retObj  = (byte[])publicMethod.invoke(obj, logger.getLogger(), this);			
			ByteArrayInputStream bis = new ByteArrayInputStream(retObj);
	        ObjectInput in = null;

	        try {
	            in = new ObjectInputStream(bis);
	            Demo object1 = (Demo)in.readObject();     

	            logger.debug("=================================================================");
	            logger.debug("Dynamice Load 내에서 호출된 것인지 확인이 필요합니다. Object has been deserialized ");
	            logger.debug("a = " + object1.a);
	            logger.debug("b = " + object1.b);
	            
	        } catch(IOException ex) {

	        } catch(ClassNotFoundException ex) {
	             System.out.println("ClassNotFoundException is caught");
	        } finally {
	            try {
	                if (in != null) {
	                in.close();
	                }
	            } catch (IOException ex) {
	                // ignore close exception
	            }
	        }
			
			// logger.debug("5.최종 처리 결과값: "+String.format("[%d][%s]", retObj.a, retObj.b));
			
		}catch(Exception e) {
			logger.debug("InvokeMethod = Exception:"+ e.getMessage());
			e.printStackTrace();
		}
		
		return retVal;
	}
    

    /*
     * ActiveSpaces 데이터 가져오기 - 테스트
     */
	public Object getInstance(String className) {
		Object object = instanceMap.get(className);
		if(object != null) return object;
		return null;
	}
	
	public String convertData(String className, String methodName, String tableName, long key, String data) {
		try {
			Object object = getInstance(className);
			if(object == null) {
				System.out.println("object is null");
				return "";
			}
			Method publicMethod = object.getClass().getMethod(methodName, String.class, long.class, String.class);
			@SuppressWarnings("unchecked")
			Map<String,String> mapData = (Map<String,String>)publicMethod.invoke(object, tableName, key, data);
			return new ObjectMapper().writeValueAsString(mapData);
		}catch(Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
