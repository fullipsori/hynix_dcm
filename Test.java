import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Test {
    public static void main(String[] args) {
        Map<String, Object> data = new ConcurrentHashMap<>();

        String handle = "message:ems";
        Optional<Object> res = data.entrySet().stream()
                .filter(entry -> handle.startsWith(entry.getKey()))
                .map(entry -> (Object) entry.getValue()).findFirst();
        String ttt = res.map(client -> {
            if (client != null)
                return "error";
            else
                return "";
        }).get();

    }
}
