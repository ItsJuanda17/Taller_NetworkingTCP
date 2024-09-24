import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Chatters {
  private Map<String, PrintWriter> users;

  public Chatters() {
    users = new HashMap<>();
  }

  public void addUser(String name, PrintWriter writer) {
    users.put(name, writer);
  }

  public void removeUser(String name) {
    users.remove(name);
  }

  public void broadCastMessage(String msg) {
    for (PrintWriter writer: users.values()) {
      writer.println(msg);
    }
  }
   
}
