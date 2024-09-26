import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Person {

    private static volatile boolean isRunning = true; // Bandera para controlar el hilo de escucha

    public Client(String userName, Socket socket) throws IOException {
        super(userName, socket);
    }

    public static void main(String[] args) {
        try {
            // Conectar al servidor en el localhost
            Socket server = new Socket("localhost", 12345);
            System.out.println("Connected to server on IP localhost and port 12345");

            // Preparar el canal de entrada/salida
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader input = new BufferedReader(new InputStreamReader(server.getInputStream()));
            PrintWriter output = new PrintWriter(server.getOutputStream(), true);

            // Solicitar nombre de usuario
            String username;
            System.out.println("Insert username:");
            username = console.readLine();
            output.println(username);

            // Crear un hilo separado para escuchar mensajes del servidor
            Thread listenThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String serverMessage;
                        while (isRunning && (serverMessage = input.readLine()) != null) {
                            System.out.println("\nServer says: " + serverMessage);
                            printMenu();
                        }
                    } catch (IOException e) {
                        if (isRunning) {
                            System.out.println("Connection closed unexpectedly.");
                        } else {
                            System.out.println("Connection closed.");
                        }
                    }
                }
            });
            listenThread.start();

            // Mientras tanto, en el hilo principal se maneja la entrada del usuario
            String choice, msg;

            while (true) {
                printMenu();
                choice = console.readLine();

                if (choice.equals("1")) {
                    System.out.println("Enter a message to send to the group: ");
                    msg = console.readLine();
                    output.println("MESSAGE " + msg);

                } else if (choice.equals("2")) {
                    System.out.println("Enter the username of the person you want to send the message to: ");
                    String to = console.readLine();
                    System.out.println("Enter the message: ");
                    msg = console.readLine();
                    output.println("PRIVATE " + to + " " + msg);

                } else if (choice.equals("3")) {
                    System.out.println("Exiting chat ...");
                    output.println("EXIT");
                    isRunning = false; // Detener el hilo de escucha
                    break;

                } else {
                    System.out.println("Invalid option. Try again.");
                }
            }

           
            console.close();
            input.close();
            output.close();
            server.close();

            // Esperar a que el hilo de escucha termine
            listenThread.join();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Método para imprimir el menú
    private static void printMenu() {
        System.out.println("\nMenu: ");
        System.out.println("1. Send message");
        System.out.println("2. Send a private message");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
    }
}
