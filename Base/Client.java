import javax.sound.sampled.*;
import java.io.*;
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
            Thread listenThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while (isRunning && (serverMessage = input.readLine()) != null) {
                        System.out.println(serverMessage);
                        if (serverMessage.startsWith("VOICE")) {
                            String[] parts = serverMessage.split(" ", 3);
                            if (parts.length == 3) {
                                String sender = parts[1];
                                if (sender.equals(username)) {
                                    continue; // No reproducir el audio grabado por el mismo usuario
                                }
                                String audioFilePath = parts[2];
                                playAudio(audioFilePath);
                            }
                        }
                    }
                } catch (IOException e) {
                    if (isRunning) {
                        System.out.println("Connection closed unexpectedly.");
                    } else {
                        System.out.println("Connection closed.");
                    }
                }
            });
            listenThread.start();

            // Mientras tanto, en el hilo principal se maneja la entrada del usuario
            String choice, msg;

            label:
            while (true) {
                printMenu();
                choice = console.readLine();

                switch (choice) {
                    case "1":
                        System.out.println("Enter a message to send to the group: ");
                        msg = console.readLine();
                        output.println(msg);
                        break;
                    case "2":
                        System.out.println("Enter the username of the person you want to send the message to: ");
                        String to = console.readLine();
                        System.out.println("Enter the message: ");
                        msg = console.readLine();
                        output.println("PRIVATE " + to + " " + msg);
                        break;
                    case "3":
                        String audioFilePath = recordAudio();
                        output.println("VOICE " + username + " " + audioFilePath);
                        break;
                    case "4":
                        System.out.println("Enter room name to create: ");
                        String roomName = console.readLine();
                        output.println("CREATE_ROOM " + roomName);
                        break;
                    case "5":
                        output.println("LIST_ROOMS");
                        System.out.println("Enter room name to join: ");
                        roomName = console.readLine();
                        output.println("JOIN_ROOM " + roomName);
                        break;
                    case "6":
                        System.out.println("Enter message to send to room: ");
                        msg = console.readLine();
                        output.println("ROOM_MSG " + msg);
                        break;
                    case "7":
                        System.out.println("Exiting chat ...");
                        output.println("EXIT");
                        isRunning = false; // Detener el hilo de escucha
                        break label;
                    default:
                        System.out.println("Invalid option. Try again.");
                        break;
                }
            }

            // Esperar a que el hilo de escucha termine
            listenThread.join();
            // Cerrar recursos
            console.close();
            input.close();
            output.close();
            server.close();

            

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void playAudio(String audioFilePath) {
        try {
            File audioFile = new File(audioFilePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
            System.out.println("Playing audio: " + audioFilePath);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String recordAudio() {
        String audioFilePath = "voiceMessage.wav"; // Nombre del archivo de audio
        AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
        try (TargetDataLine line = AudioSystem.getTargetDataLine(format)) {
            line.open(format);
            line.start();
            System.out.println("Recording... Press ENTER to stop.");
            try (AudioInputStream audioInputStream = new AudioInputStream(line)) {
                Thread recordingThread = new Thread(() -> {
                    try {
                        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File(audioFilePath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                recordingThread.start();
                System.in.read(); // Esperar a que se presione ENTER
                System.in.read();
                line.stop();
                line.close();
                recordingThread.join(); // Esperar a que termine la grabación
            }
            System.out.println("Recording saved as " + audioFilePath);
        } catch (LineUnavailableException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return audioFilePath;
    }

    // Método para imprimir el menú
    private static void printMenu() {
        System.out.println("""
                Choose an option:
                1. Send message
                2. Send a private message
                3. Record a voice message
                4. Create a chat room
                5. Join a chat room
                6. Send message to room
                7. Exit
                """);
    }
}
