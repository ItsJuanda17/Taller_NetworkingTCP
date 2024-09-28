import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client extends Person {

    private static volatile boolean isRunning = true; // Bandera para controlar el hilo de escucha
    private static volatile boolean inCall = false;
    private static String currentRoom = null;

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
                        String[] parts = serverMessage.split(" ", 4);
                        boolean isVoiceMessage = parts[0].startsWith("VOICE") || parts[0].startsWith("Voice")
                                || parts[0].startsWith("VOICE_ROOM");
                        if (!isVoiceMessage) {
                            System.out.println(serverMessage);
                        }
                        if (parts.length == 3) {
                            if (parts[0].equals("VOICE")) {
                                String sender = parts[1];
                                if (sender.equals(username)) {
                                    continue;
                                }
                                System.out.println("Voice message from: " + parts[1]);
                                String voiceData = parts[2];
                                playVoiceData(voiceData);
                            } else if (parts[0].equals("Joined room:")) {
                                currentRoom = parts[1].trim();
                                System.out.println("You are now in room: " + currentRoom);
                            }
                        } else if (parts.length == 4) {
                            if (parts[0].equals("VOICE_ROOM")) {
                                String sender = parts[2];
                                if (sender.equals(username)) {
                                    continue;
                                }
                                if (currentRoom == null) {
                                    continue;
                                }
                                if (currentRoom.equals(parts[1])) {
                                    System.out.println("Voice message from: " + parts[2]);
                                    String voiceData = parts[3];
                                    playVoiceData(voiceData);
                                }
                            }
                        } else {
                            System.out.println(serverMessage);
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

            label: while (true) {
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
                        recordAndSendAudio(output, username, false);
                        break;
                    case "4":
                        System.out.println("Enter room name to create: ");
                        String roomName = console.readLine();
                        currentRoom = roomName;
                        output.println("CREATE_ROOM " + roomName);
                        break;
                    case "5":
                        output.println("LIST_ROOMS");
                        System.out.println("Enter room name to join: ");
                        roomName = console.readLine();
                        output.println("JOIN_ROOM " + roomName);
                        currentRoom = roomName;
                        break;
                    case "6":
                        if (currentRoom != null) {
                            System.out.println("Enter message to send to room: ");
                            msg = console.readLine();
                            output.println("ROOM_MSG " + msg);
                        } else {
                            System.out.println("You are not in any room. Join a room first.");
                        }
                        break;
                    case "7":
                        if (currentRoom != null) {
                            recordAndSendAudio(output, username, true);
                            System.in.read();
                        } else {
                            System.out.println("Error: You are not in any room.");
                        }
                        break;
                    case "8":
                        if (currentRoom != null) {
                            output.println("GET_HISTORY " + currentRoom);
                        } else {
                            System.out.println("Error: You are not in any room.");
                        }
                        break;
                    case "9":
                        System.out.println("Enter the username to call:");
                        String callUser = console.readLine();
                        voiceCall(output, input, callUser); // Iniciar llamada de voz
                        break;
                    case "10":
                        startVoiceCall(output, input, username);
                        break;
                    case "11":
                        rejectCall(output);
                        break;
                    case "12":
                        endCall(output);
                        break;
                    case "13":
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

    private static void recordAndSendAudio(PrintWriter output, String username, boolean security) {
        boolean room_message = security;
        AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
        AtomicBoolean recording = new AtomicBoolean(true); // Bandera para controlar la grabación

        try (TargetDataLine line = AudioSystem.getTargetDataLine(format)) {
            line.open(format);
            line.start();
            System.out.println("Recording... Press ENTER to stop.");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // Hilo para grabar el audio
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

            // Esperar a que el usuario presione ENTER
            System.in.read(); // Espera ENTER para detener la grabación

            // Detener la grabación
            recording.set(false); // Cambiar el estado de la bandera
            line.stop();
            line.close();
            recordingThread.join();

            // Convertir los datos grabados a un array de bytes
            byte[] audioData = byteArrayOutputStream.toByteArray();

            // Codificar los datos en Base64 y enviarlos
            byte[] encodedBytes = Base64.getEncoder().encode(audioData);

            if (room_message) {
                output.println("VOICE_ROOM " + currentRoom + " " + username + " " + new String(encodedBytes));
            } else {
                output.println("VOICE " + new String(encodedBytes));
            }

            System.out.println("Audio sent successfully!");

        } catch (LineUnavailableException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Método para iniciar la llamada en tiempo real
    public static void voiceCall(PrintWriter output, BufferedReader input, String targetUser) {
        System.out.println("Calling " + targetUser + "...");
        output.println("CALL " + targetUser); // Enviamos una solicitud de llamada al usuario objetivo
    }

    public static void startVoiceCall(PrintWriter output, BufferedReader input, String targetUser) throws IOException {
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

    public static void rejectCall(PrintWriter output) {
        inCall = false;
        output.println("REJECT");
        System.out.println("Call rejected.");
    }

    // Método para enviar audio en tiempo real
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

    // Método para terminar la llamada
    public static void endCall(PrintWriter output) {
        inCall = false;
        output.println("END_CALL");
        System.out.println("Call ended.");
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
