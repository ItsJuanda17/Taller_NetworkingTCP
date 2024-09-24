import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class Client extends Person {

    public Client(String userName, Socket socket) throws IOException {
        super(userName, socket);
    }

    public static void main(String[] args) {
        try {
            // Conectar al servidor en el localhost, se puede reemplazar por 127.0.0.1, puerto 12345 <- Este está definido en el server
            Socket server = new Socket("localhost", 12345);
            System.out.println("Connected to server on IP localhost and port 12345");

            // Preparar el canal de entrada/salida
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader input = new BufferedReader(new InputStreamReader(server.getInputStream()));
            PrintWriter output = new PrintWriter(server.getOutputStream(), true);

            String username, msg;
            System.out.println("Insert username:");
            username = console.readLine();
            output.println(username);

            while (true) {
                System.out.print("Enter a message to send to the server: ");
                msg = console.readLine();

                if (msg.equals("exit")) {
                    break;
                }

                // Enviar mensaje al servidor
                output.println(msg);

                // Leer respuesta del servidor
                System.out.println("Server says: " + input.readLine());
            }

            // Cerrar recursos
            console.close();
            input.close();
            output.close();
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
