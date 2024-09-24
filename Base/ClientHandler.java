import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
  private Socket client;
  private Chatters chatters;
  private String username;

  public ClientHandler(Socket client, Chatters chatters) {
    this.client = client;
    this.chatters = chatters;
  }

  @Override
  public void run() {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
      PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
      username = reader.readLine();
      chatters.addUser(username, writer);

      String msg;
      while ((msg = reader.readLine()) != null) {
        System.out.println(username + ": " + msg);
        chatters.broadCastMessage(username + ": " + msg);
      }

    } catch (IOException e) {
      System.out.println(e.getMessage());
    } finally {
      chatters.removeUser(username);
    }
  }
}