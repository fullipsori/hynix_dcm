import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Test extends BaseTest {

    public Test() {
        System.out.println("child");
    }

    public static void main(String[] args) {
        Test test = new Test();
    }
}
