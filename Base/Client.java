import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client extends Person {

    private static volatile boolean isRunning = true; // Bandera para controlar el hilo de escucha
    private static volatile boolean inCall = false; // Bandera para indicar si está en llamada
    private static String currentRoom = null; // Almacena la sala actual

    public Client(String userName, Socket socket) throws IOException {
        super(userName, socket);
    }

    public static void main(String[] args) {
        try {
            Socket server = connectToServer(); // Conectar al servidor
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader input = new BufferedReader(new InputStreamReader(server.getInputStream()));
            PrintWriter output = new PrintWriter(server.getOutputStream(), true);

            String username = requestUsername(console, output); // Solicitar nombre de usuario
            Thread listenThread = startListeningThread(input, username); // Iniciar hilo de escucha

            handleUserInput(console, output); // Manejar la entrada del usuario

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

    private static Socket connectToServer() throws IOException {
        Socket server = new Socket("localhost", 12345);
        System.out.println("Connected to server on IP localhost and port 12345");
        return server;
    }

    private static String requestUsername(BufferedReader console, PrintWriter output) throws IOException {
        System.out.println("Insert username:");
        String username = console.readLine();
        output.println(username);
        return username;
    }

    private static Thread startListeningThread(BufferedReader input, String username) {
        Thread listenThread = new Thread(() -> {
            try {
                String serverMessage;
                while (isRunning && (serverMessage = input.readLine()) != null) {
                    handleServerMessage(serverMessage, username); // Manejar mensajes del servidor
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
        return listenThread;
    }

    private static void handleServerMessage(String serverMessage, String username) {
        String[] parts = serverMessage.split(" ", 4);
        boolean isVoiceMessage = parts[0].startsWith("VOICE") || parts[0].startsWith("Voice")
                || parts[0].startsWith("VOICE_ROOM");

        if (!isVoiceMessage) {
            System.out.println(serverMessage);
        }

        if (parts.length == 3) {
            if (parts[0].equals("VOICE")) {
                handleVoiceMessage(parts, username);
            } else if (parts[0].equals("Joined room:")) {
                currentRoom = parts[1].trim();
                System.out.println("You are now in room: " + currentRoom);
            }
        } else if (parts.length == 4) {
            if (parts[0].equals("VOICE_ROOM")) {
                handleRoomVoiceMessage(parts, username);
            }
        } else {
            System.out.println(serverMessage);
        }
    }

    private static void handleVoiceMessage(String[] parts, String username) {
        String sender = parts[1];
        if (sender.equals(username)) {
            return;
        }
        System.out.println("Voice message from: " + sender);
        String voiceData = parts[2];
        playVoiceData(voiceData);
    }

    private static void handleRoomVoiceMessage(String[] parts, String username) {
        String sender = parts[2];
        if (sender.equals(username) || currentRoom == null || !currentRoom.equals(parts[1])) {
            return;
        }
        System.out.println("Voice message from: " + sender);
        String voiceData = parts[3];
        playVoiceData(voiceData);
    }

    private static void handleUserInput(BufferedReader console, PrintWriter output) throws IOException {
        String choice;
        label: while (true) {
            printMenu();
            choice = console.readLine();

            switch (choice) {
                case "1":
                    sendMessageToGroup(console, output);
                    break;
                case "2":
                    sendPrivateMessage(console, output);
                    break;
                case "3":
                    recordAndSendAudio(output, currentRoom != null);
                    break;
                case "4":
                    createChatRoom(console, output);
                    break;
                case "5":
                    joinChatRoom(console, output);
                    break;
                case "6":
                    sendMessageToRoom(console, output);
                    break;
                case "7":
                    sendVoiceMessageToRoom(output);
                    break;
                case "8":
                    viewRoomHistory(output);
                    break;
                case "9":
                    callUser(console, output);
                    break;
                case "10":
                    acceptCall(console, output);
                    break;
                case "11":
                    rejectCall(output);
                    break;
                case "12":
                    endCall(output);
                    break;
                case "13":
                    exitChat(output);
                    break label;
                default:
                    System.out.println("Invalid option. Try again.");
                    break;
            }
        }
    }

    private static void sendMessageToGroup(BufferedReader console, PrintWriter output) throws IOException {
        System.out.println("Enter a message to send to the group: ");
        String msg = console.readLine();
        output.println(msg);
    }

    private static void sendPrivateMessage(BufferedReader console, PrintWriter output) throws IOException {
        System.out.println("Enter the username of the person you want to send the message to: ");
        String to = console.readLine();
        System.out.println("Enter the message: ");
        String msg = console.readLine();
        output.println("PRIVATE " + to + " " + msg);
    }

    private static void createChatRoom(BufferedReader console, PrintWriter output) throws IOException {
        System.out.println("Enter room name to create: ");
        String roomName = console.readLine();
        currentRoom = roomName;
        output.println("CREATE_ROOM " + roomName);
    }

    private static void joinChatRoom(BufferedReader console, PrintWriter output) throws IOException {
        output.println("LIST_ROOMS");
        System.out.println("Enter room name to join: ");
        String roomName = console.readLine();
        output.println("JOIN_ROOM " + roomName);
        currentRoom = roomName;
    }

    private static void sendMessageToRoom(BufferedReader console, PrintWriter output) throws IOException {
        if (currentRoom != null) {
            System.out.println("Enter message to send to room: ");
            String msg = console.readLine();
            output.println("ROOM_MSG " + msg);
        } else {
            System.out.println("You are not in any room. Join a room first.");
        }
    }

    private static void sendVoiceMessageToRoom(PrintWriter output) throws IOException {
        if (currentRoom != null) {
            recordAndSendAudio(output, true);
            System.in.read();
        } else {
            System.out.println("Error: You are not in any room.");
        }
    }

    private static void viewRoomHistory(PrintWriter output) {
        if (currentRoom != null) {
            output.println("GET_HISTORY " + currentRoom);
        } else {
            System.out.println("Error: You are not in any room.");
        }
    }

    private static void callUser (BufferedReader console, PrintWriter output) throws IOException {
        System.out.println("Enter the username of the person you want to call: ");
        String targetUser = console.readLine();
        voiceCall(output, targetUser);
    }

    private static void acceptCall(BufferedReader console, PrintWriter output) {
        startVoiceCall(output, console, output.toString()); // Debes pasar el usuario correcto aquí
    }

    private static void exitChat(PrintWriter output) {
        System.out.println("Exiting chat ...");
        output.println("EXIT");
        isRunning = false; // Detener el hilo de escucha
    }

    // Métodos de audio y llamadas

    private static void playVoiceData(String base64Data) {
        try {
            byte[] audioBytes = Base64.getDecoder().decode(base64Data);
            InputStream byteArrayInputStream = new ByteArrayInputStream(audioBytes);
            AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
            AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream, format,
                    audioBytes.length / format.getFrameSize());

            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método ajustado para grabar y enviar audio
    private static void recordAndSendAudio(PrintWriter output, boolean isRoomMessage) {
        AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
        AtomicBoolean recording = new AtomicBoolean(true);

        try (TargetDataLine line = AudioSystem.getTargetDataLine(format)) {
            line.open(format);
            line.start();
            System.out.println("Recording... Press ENTER to stop.");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            Thread recordingThread = getThread(line, recording, byteArrayOutputStream);

            // Esperar a que el usuario presione ENTER
            System.in.read(); // Espera ENTER para detener la grabación

            // Detener la grabación
            recording.set(false);
            line.stop();
            line.close();
            recordingThread.join();

            byte[] audioData = byteArrayOutputStream.toByteArray();
            byte[] encodedBytes = Base64.getEncoder().encode(audioData);

            if (isRoomMessage) {
                output.println("VOICE_ROOM " + currentRoom + " " + new String(encodedBytes));
            } else {
                output.println("VOICE " + new String(encodedBytes));
            }

            System.out.println("Audio sent successfully!");

        } catch (LineUnavailableException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Thread getThread(TargetDataLine line, AtomicBoolean recording, ByteArrayOutputStream byteArrayOutputStream) {
        Thread recordingThread = new Thread(() -> {
            try (AudioInputStream audioInputStream = new AudioInputStream(line)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while (recording.get() && (bytesRead = audioInputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        recordingThread.start();
        return recordingThread;
    }

    public static void voiceCall(PrintWriter output, String targetUser) {
        System.out.println("Calling " + targetUser + "...");
        output.println("CALL " + targetUser); // Enviamos una solicitud de llamada al usuario objetivo
    }

    // Método para recibir audio en tiempo real
    private static void receiveRealTimeAudio(BufferedReader input) throws IOException {
        System.out.println("Receiving real-time audio...");

        String serverMessage;
        while (inCall && (serverMessage = input.readLine()) != null) {
            String[] parts = serverMessage.split(" ", 3);
            if (parts.length == 3 && "VOICE".equals(parts[0])) {
                String encodedAudio = parts[2]; // Obtener el audio codificado
                playVoiceData(encodedAudio); // Reproducir el audio recibido
            }
        }

        System.out.println("Stopped receiving audio.");
    }

    private static void sendRealTimeAudio(PrintWriter output) {
        AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
        try (TargetDataLine microphone = AudioSystem.getTargetDataLine(format)) {
            microphone.open(format);
            microphone.start();

            byte[] buffer = new byte[4096]; // Buffer para almacenar el audio
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            System.out.println("Streaming audio...");

            while (inCall) {
                int bytesRead = microphone.read(buffer, 0, buffer.length); // Capturar audio en el buffer
                if (bytesRead > 0) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    byte[] encodedAudio = Base64.getEncoder().encode(byteArrayOutputStream.toByteArray());
                    output.println("VOICE " + new String(encodedAudio)); // Enviar audio codificado
                    byteArrayOutputStream.reset(); // Limpiar el stream
                }
            }

            microphone.stop();
            microphone.close();
            System.out.println("Audio streaming ended.");

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void startVoiceCall(PrintWriter output, BufferedReader input, String targetUser) {
        System.out.println(targetUser + " accepted the call. Starting voice communication...");
        inCall = true;

        // Hilo para enviar audio de manera continua
        Thread sendAudioThread = new Thread(() -> {
            try {
                sendRealTimeAudio(output); // Método para enviar audio
            } catch (Exception e) {
                System.out.println("Error during sending audio: " + e.getMessage());
            }
        });

        // Hilo para recibir audio de manera continua
        Thread receiveAudioThread = new Thread(() -> {
            try {
                receiveRealTimeAudio(input); // Método para recibir audio
            } catch (Exception e) {
                System.out.println("Error during receiving audio: " + e.getMessage());
            }
        });

        // Iniciar ambos hilos
        sendAudioThread.start();
        receiveAudioThread.start();

        try {
            // Unir ambos hilos cuando la llamada termine
            sendAudioThread.join();
            receiveAudioThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }


    public static void endCall(PrintWriter output) {
        inCall = false;
        output.println("END_CALL");
        System.out.println("Call ended.");
    }

    public static void rejectCall(PrintWriter output) {
        inCall = false;
        output.println("REJECT");
        System.out.println("Call rejected.");
    }

    private static void printMenu() {
        System.out.println("""
                Choose an option:
                1. Send message
                2. Send a private message
                3. Record a voice message
                4. Create a chat room
                5. Join a chat room
                6. Send message to room
                7. Send voice message to room
                8. View room history
                9. Call a user
                10. Accept call
                11. Reject call
                12. End call
                13. Exit chat
                """);
    }
}
