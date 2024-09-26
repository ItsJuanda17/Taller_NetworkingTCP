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

  public void privateMessage(String from, String to, String msg) {
    PrintWriter recipientWriter = users.get(to);
    
    if (recipientWriter != null) {
        recipientWriter.println("Private message from " + from + ": " + msg);
    } else {
        // Si no se encuentra el destinatario, enviar un mensaje de error de vuelta al remitente
        PrintWriter senderWriter = users.get(from);
        if (senderWriter != null) {
            senderWriter.println("Error: User " + to + " not found.");
        }
    }
}


}