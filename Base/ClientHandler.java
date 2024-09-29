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
                        String recipient = splitMsg[1]; // El segundo elemento es el destinatario

                        String content = splitMsg[2]; // El tercer elemento es el contenido del mensaje

                        if (recipient.equals(username)) {
                            writer.println("Error: No puedes enviarte mensajes privados a ti mismo.");
                        } else {
                            chatters.privateMessage(username, recipient, content);
                        }
                        break;
                    }
                    case "PRIVATE_VOICE": {
                        String recipient = splitMsg[1]; // Usuario destinatario

                        String voiceData = splitMsg[2]; // Datos del mensaje de voz

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
                        String voiceByteData = voiceData[2];
                        if (currentRoom != null) {
                            chatters.sendVoiceMessageToRoom(currentRoom, username, voiceByteData); // Enviar voz a sala

                        }
                        break;
                    }
                    case "CREATE_ROOM": {
                        String roomName = splitMsg[1];
                        chatters.createRoom(roomName, username); // Crear y unirse automáticamente

                        currentRoom = roomName;
                        writer.println("Created and joined room: " + roomName);
                        break;
                    }
                    case "JOIN_ROOM": {
                        String roomName = splitMsg[1];
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
                        String recipient = splitMsg[1];
                        if (chatters.userExists(recipient)) {
                            chatters.callUser(username, recipient);
                        } else {
                            writer.println("Error: User " + recipient + " not found.");
                        }
                        break;
                    }
                    case "CALL_ACCEPTED": {
                        String caller = splitMsg[1];
                        if (inCall) {
                            writer.println("Error: You are already in a call.");
                        } else {
                            chatters.acceptCall(username, caller);
                            inCall = true;
                            callRecipient = caller;
                        }
                        break;
                    }
                    case "CALL_REJECTED": {
                        String caller = splitMsg[1];
                        chatters.rejectCall(username, caller);
                        break;
                    }
                    case "CALL_AUDIO": {
                        String audioData = splitMsg[2];
                        if (inCall) {
                            chatters.sendCallAudio(username, callRecipient, audioData);
                        } else {
                            writer.println("Error: You are not in a call.");
                        }
                        break;
                    }
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