import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Method;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;

// EMS 관련 패키지
import com.tibco.tibjms.*;
import javax.jms.*;


// KAFKA 관련 패키지
import org.apache.kafka.clients.producer.Producer; 			// import simple producer packages
import org.apache.kafka.clients.producer.KafkaProducer; 	// import KafkaProducer packages
import org.apache.kafka.clients.producer.ProducerRecord; 	// import ProducerRecord packages
import org.apache.kafka.common.*;

// import com.skhynix.narf.ILogManager;
// import com.skhynix.narf.LogManager;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public class MRtest {

    // static Logger LOGGER = LoggerFactory.getLogger(MRtest.class);

    public static void main(String[] args) {
        System.out.println("Hello World-MR Test-성공적으로 Message Routing 연결을 하였습니다.");        
        MRtest t = new MRtest();
        // com.skhynix.narf.LogManager test = new com.skhynix.narf.LogManager();
        String jsonFilePath = "D:\\00_SKhynix\\test-project\\test-db\\test.json";
        // t.doBusiness(test, t);
        String retVal = "변경되었음";
        System.out.println("최종결과: "+retVal);
    }

    public MRtest() {
        System.out.println("MRtest 생성자가 실행되었습니다.");
    }

    public void printTestForTestInvoke(String value) {
        logger.log(Level.INFO," printTestForTestInvoke 인자값 ["+value+"]");
    }

    // private static LogManager lm = new LogManager();
    private Map<String, Object> instanceMap = null;    
    private static Logger logger = null;
    public Object doBusiness(Object logManager, Object DLM) {

        logger = (Logger)logManager;

        String OS = System.getProperty("os.name");
        String args = "D:\\00_SKhynix\\test-project\\test-db\\test.json";
        int value = 10;
		
        if(!OS.startsWith("Windows")) {
            args = "/home/tibco/skhynix/dynamic-jar/test.json";
        }

        logger.log(Level.INFO,"1) MRTest Class. void printTest(String args, int value) Dynamic Jar Loading Test ["+args+"]["+value+"]");
        logger.log(Level.INFO,"*************************************************************");        
        String methodName = "debug";

        logger.log(Level.INFO,"메시지 전달해보기 이상이 있는지 확인해본다.");

        // LOGGER.info("테스트가 제대로 이루어졌는지 확인한다.");

		logger.log(Level.INFO," 3. 버전 변경 여부 - Invoke Public Method : MDMtest."+methodName+"(Object, Integer)");
        try {            
            logger.log(Level.INFO,"4.1");                 
            logger.log(Level.INFO,"4.2");     
            Method getMethod = DLM.getClass().getDeclaredMethod("getInstanceMap", String.class);            
		    logger.log(Level.INFO,"&&&&&&&&&&&&& 4.3 Invoke Public Method : MRtest."+methodName+"(Object, Integer)");            
            instanceMap = (Map<String, Object>)getMethod.invoke(DLM, "test message");
            System.out.println("4.4 인스턴스 맵을 가져와서 필요한 모든 설정을 진행한다.");     
            logger.log(Level.INFO,"4.5 get successed");            
            Object valuesMRTest = instanceMap.get("MRtest");

            if(valuesMRTest != null ) {
                logger.log(Level.INFO,"5.1 value get OK");                
                ((MRtest)valuesMRTest).printTestForTestInvoke("제대로 된 값이 넘어가는지 확인한다.");                
                logger.log(Level.INFO,"5.2");
            } else {
                logger.log(Level.INFO,"5.3*** 문제가 발생하였습니다. 해당 값이 없습니다.");
            }           
        }catch(Exception e) {
            e.printStackTrace();
        }
        
        // EMS 메시지 전송        
		emsMsgProducer();	
        // FTL 메시지 전송
        // ftlMessageSender();		
		// KAFKA 메시지 전송        
		kafkaProducer();

        return "[welldone]";
    }

    public void writeJSON() {
        JSONObject obj = new JSONObject();
        System.out.println("** start to write JSON File");
        obj.put("name", "Edward Won In Tibco");
        obj.put("age", 100);

        JSONArray list = new JSONArray();
        list.add("msg 1");
        list.add("msg 2");
        list.add("msg 3");

        obj.put("messages", list);

        try (FileWriter file = new FileWriter(".\\test.json")) {
            file.write(obj.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print(obj);
    }

    private static JSONObject jsonObject = null;
    public String readJSON(String args){

        String retVal  = "[JSON] 초기화";

        try {
            JSONParser parser = new JSONParser();
            Reader reader = new FileReader(args);
            
            jsonObject = (JSONObject) parser.parse(reader);
            
            String name = (String) jsonObject.get("name");
            long age = (Long) jsonObject.get("age");            

            // loop array
            JSONArray msg = (JSONArray) jsonObject.get("messages");
            Iterator<String> iterator = msg.iterator();
            while (iterator.hasNext()) {
                System.out.println(iterator.next());
            }

        } catch (IOException e) {            
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            
        }
        return retVal;
    }

    // EMS Message 보내기 예제
    String          serverUrl    = null;
    String          userName     = null;
    String          password     = null;
    String          name         = "topic.sample";
    Vector<String>  data         = new Vector<String>();
    boolean         useTopic     = true;
    boolean         useAsync     = false;

    /*-----------------------------------------------------------------------
     * Variables
     *----------------------------------------------------------------------*/
    Connection      connection   = null;
    Session         session      = null;
    MessageProducer msgProducer  = null;
    Destination     destination  = null;

	public String emsMsgProducer( ) {

        String retVal  = "ems 1.";
		try 
        {
            TextMessage msg;            
            System.err.println("Publishing to destination '"+name+"'\n");
            serverUrl="tcp://192.168.232.133:7222";
            ConnectionFactory factory = new com.tibco.tibjms.TibjmsConnectionFactory(serverUrl);

            connection = factory.createConnection("admin",password);
            session = connection.createSession(javax.jms.Session.AUTO_ACKNOWLEDGE);

            destination = session.createQueue(name);

            msgProducer = session.createProducer(null);

            msg = session.createTextMessage();

            String strMsg = "테스트 메시지 보내기 - EMS in TestJava";
            msg.setText(strMsg);

            msgProducer.send(destination, msg);
            System.err.println("Published message: "+strMsg);
            connection.close();
        } 
        catch (JMSException e) 
        {
            e.printStackTrace();
            System.exit(-1);
        }

        return retVal;
	}
	
	int countValue = 0;	
	public String kafkaProducer() {
		  // create instance for properties to access producer configs   
          String retVal = "kafak Initi";
		  Properties props = new Properties();
		  
		  //Assign localhost id
          retVal = "k1 "+retVal;
		  props.put("bootstrap.servers", "192.168.232.133:9192");
		  
		  //Set acknowledgements for producer requests.                
		  props.put("acks", "all");
		  
		  //If the request fails, the producer can automatically retry,
		  props.put("retries", 0);
		  
		  //Specify buffer size in config
		  props.put("batch.size", 16384);
		  
		  //Reduce the no of requests less than 0   
		  props.put("linger.ms", 1);
		  
		  //The buffer.memory controls the total amount of memory available to the producer for buffering.   
		  props.put("buffer.memory", 33554432);		

          Thread.currentThread().setContextClassLoader(null);  
		  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");		     
		  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
          
         System.out.println("*************************************");	      
         System.out.println("1. Creating KafakProducer!!!");
         Producer<String, String> producer = null;
        try{ 
            retVal = "k2 "+retVal;
            producer = new KafkaProducer<String, String>(props);
            System.out.println("2. Producer created successfually!!!");	      
            retVal = "k3 "+retVal;
        }catch(Exception e) {
            retVal = "k4.1 오류 발생"+ retVal;
        }
		
		String topicName = "quickstart-events";
		  
        System.out.println("*************************************");	      
        retVal = "k4 "+retVal;
        producer.send(new ProducerRecord<String, String>(topicName, "MRTest.java", String.format("[완성된 MRTest] Test Message: 한글처리가 제대로 되는지 확인한다. [%06d]", countValue++)));
            
        
        retVal = "k5 "+retVal;
        System.out.println("3. Message sent successfully");
        System.out.println("*************************************");
        producer.close();	      
        retVal = "k6 "+retVal;

        return retVal;
	}
}