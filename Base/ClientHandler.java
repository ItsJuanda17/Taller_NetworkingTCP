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
            // Verificar si el mensaje es para enviar un mensaje privado
            if (msg.startsWith("PRIVATE")) {
                String[] splitMsg = msg.split(" ", 3); // Cambiar a 3 para incluir el contenido del mensaje
                if (splitMsg.length == 3) {
                    String recipient = splitMsg[1].trim(); // El segundo elemento es el destinatario
                    String content = splitMsg[2].trim(); // El tercer elemento es el contenido del mensaje
                    chatters.privateMessage(username, recipient, content);
                } else {
                    writer.println("Error: Formato de mensaje privado incorrecto.");
                }

            } else if (msg.equals("EXIT")) {
                break; // Salir del bucle y cerrar la conexión
            } else {
                // Si no es un mensaje privado, lo enviamos al grupo
                chatters.broadCastMessage(username + ": " + msg);
            }
        }

    } catch (IOException e) {
        System.out.println(e.getMessage());
    } finally {
        chatters.removeUser(username); // Eliminar el usuario al salir
    }
}


  
}