import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerTCP {

    private static final int PORT = 12345;
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {

        ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket server = new ServerSocket(PORT)) { // Uso try-with-resources para asegurar que se cierre el ServerSocket
            System.out.println("Server is running on port " + PORT);

            while (true) {
                // Aceptar la conexión de un cliente
                Socket client = server.accept();
                System.out.println("Client connected from " + client.getInetAddress().getHostAddress());

                // Ejecutar la tarea del cliente en un hilo del pool
                pool.execute(new ClientHandler(client));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


class ClientHandler implements Runnable {
    private Socket client;

    public ClientHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try (
            // Preparar el canal de entrada/salida
            BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter output = new PrintWriter(client.getOutputStream(), true)
        ) {
            // Leer mensajes del cliente
            String ms;
            while ((ms = input.readLine()) != null) {
                System.out.println("Client says: " + ms);

                // Responder en mayúsculas
                String response = ms.toUpperCase();
                output.println("Server says: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close(); // Asegurar que el socket del cliente se cierre
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
