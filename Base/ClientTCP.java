import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientTCP {
    public static void main(String[] args) {
        try {
            // Conectar al servidor en la IP 192.168.137.1, puerto 12345
            Socket server = new Socket("172.30.175.156", 12345);
            System.out.println("Connected to server on IP 192.168.137.1 and port 4321");

            // Preparar el canal de entrada/salida
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader input = new BufferedReader(new InputStreamReader(server.getInputStream()));
            PrintWriter output = new PrintWriter(server.getOutputStream(), true);

            String ms;
            while (true) {
                System.out.print("Enter a message to send to the server: ");
                ms = console.readLine();

                if (ms.equals("exit")) {
                    break;
                }

                // Enviar mensaje al servidor
                output.println(ms);

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
