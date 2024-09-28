import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket client;
    private final Chatters chatters;
    private String username;
    private String currentRoom;
    private boolean inCall;
    private String callRecipient;

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
            label:
            while ((msg = reader.readLine()) != null) {
                // Verificar si el mensaje es para enviar un mensaje privado
                String[] splitMsg = msg.split(" ", 4);
                switch (splitMsg[0]) {
                    case "PRIVATE": {
                        String recipient = splitMsg[1].trim(); // El segundo elemento es el destinatario

                        String content = splitMsg[2].trim(); // El tercer elemento es el contenido del mensaje

                        if (recipient.equals(username)) {
                            writer.println("Error: No puedes enviarte mensajes privados a ti mismo.");
                        } else {
                            chatters.privateMessage(username, recipient, content);
                        }
                        break;
                    }
                    case "PRIVATE_VOICE": {
                        String recipient = splitMsg[1].trim();  // Usuario destinatario

                        String voiceData = splitMsg[2].trim();  // Datos del mensaje de voz

                        if (recipient.equals(username)) {
                            writer.println("Error: No puedes enviarte mensajes de voz a ti mismo.");
                        } else {
                            chatters.sendPrivateVoiceMessage(username, recipient, voiceData);
                        }

                        break;
                    }
                    case "EXIT":
                        // Salir del bucle y cerrar la conexión removiéndolo de la lista de usuarios
                        chatters.removeUser(username);
                        break label;
                    case "VOICE": {
                        String voiceData = splitMsg[1];
                        chatters.broadCastMessage("VOICE " + username + " " + voiceData);
                        break;
                    }
                    case "VOICE_ROOM": {
                        String[] voiceData = msg.split(" ", 4);
                        String voiceByteData = voiceData[3];
                        if (currentRoom != null) {
                            chatters.sendVoiceMessageToRoom(currentRoom, username, voiceByteData); // Enviar voz a sala

                        }
                        break;
                    }
                    case "CREATE_ROOM": {
                        String roomName = splitMsg[1].trim();
                        chatters.createRoom(roomName, username); // Crear y unirse automáticamente

                        currentRoom = roomName;
                        writer.println("Created and joined room: " + roomName);
                        break;
                    }
                    case "JOIN_ROOM": {
                        String roomName = splitMsg[1].trim();
                        if (chatters.roomExists(roomName)) {
                            chatters.addUserToRoom(roomName, username);
                            currentRoom = roomName;
                            writer.println("Joined room: " + roomName);
                        } else {
                            writer.println("Error: Room " + roomName + "does not exist. ");

                        }

                        break;
                    }
                    case "LIST_ROOMS":
                        writer.println("Rooms available:  " + chatters.getRooms());
                        break;
                    case "ROOM_MSG":
                        if (currentRoom != null) {
                            String content = msg.split(" ", 2)[1];
                            chatters.broadcastToRoom(currentRoom, username + ": " + content);
                        } else {
                            writer.println("Error: You are not in any room.");
                        }
                        break;
                    case "GET_HISTORY":
                        if (currentRoom != null) {
                            writer.println("Room history: " + chatters.getRoomHistory(currentRoom));
                        } else {
                            writer.println("Error: You are not in any room.");
                        }
                        break;
                    case "CALL": {
                        String recipient = splitMsg[1].trim();
                        if (chatters.userExists(recipient)) {
                            PrintWriter recipientWriter = chatters.getWriter(recipient);
                            recipientWriter.println(username + " is calling you. Type ACCEPT or REJECT");
                            String recipientResponse = reader.readLine();
                            if ("ACCEPT".equals(recipientResponse)) {
                                inCall = true;
                                callRecipient = recipient;
                                writer.println("ACCEPT");
                                recipientWriter.println("Call Accepted. Starting voice chat.");
                            } else if ("REJECT".equals(recipientResponse)) {
                                writer.println("REJECT");
                                recipientWriter.println("Call Rejected.");
                            }
                        } else {
                            writer.println("User did not respond.");
                        }
                        break;
                    }
                    case "END_CALL":
                        if (inCall) {
                            PrintWriter recipientWriter = chatters.getWriter(callRecipient);
                            recipientWriter.println("END_CALL");
                            inCall = false;
                            callRecipient = null;
                            break label;
                        }
                        break;
                    default:
                        // Si no es un mensaje privado, lo enviamos al grupo
                        chatters.broadCastMessage(username + ": " + msg);
                        break;
                }
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                chatters.removeUser(username);
                if (client != null) {
                    client.close();
                }

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

}