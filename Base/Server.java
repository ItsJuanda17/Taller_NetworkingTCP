import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORT = 12345;

    public static void main(String[] args) throws IOException {

        Chatters chatters = new Chatters();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port: " + PORT);
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Cliente conectado desde " + client.getInetAddress());
                ClientHandler handler = new ClientHandler(client, chatters);
                Thread thread = new Thread(handler);
                thread.start();
            }
        }
    }
}