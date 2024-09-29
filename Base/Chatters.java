import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Chatters {
    private final Map<String, PrintWriter> users;
    private final Map<String, List<String>> chatHistories;
    private final Map<String, Map<String, PrintWriter>> chatRooms;

    public Chatters() {
        users = new HashMap<>();
        chatRooms = new HashMap<>();
        chatHistories = new HashMap<>();
    }

    public void addUser(String name, PrintWriter writer) {
        users.put(name, writer);
    }

    public void removeUser(String name) {
        users.remove(name);
        for (Map<String, PrintWriter> room : chatRooms.values()) {
            room.remove(name);
        }
    }

    // Broadcast global a todos los usuarios
    public void broadCastMessage(String msg) {
        for (PrintWriter writer : users.values()) {
            writer.println(msg);
        }
    }

    // Mensaje privado entre dos usuarios
    public void privateMessage(String from, String to, String msg) {
        PrintWriter recipientWriter = users.get(to);
        if (recipientWriter != null) {
            recipientWriter.println("Private message from " + from + ": " + msg);
        } else {
            PrintWriter senderWriter = users.get(from);
            if (senderWriter != null) {
                senderWriter.println("Error: User " + to + " not found.");
            }
        }
    }

    // Método para enviar un mensaje de voz privado entre dos usuarios
    public void sendPrivateVoiceMessage(String from, String to, String voiceData) {
        PrintWriter recipientWriter = users.get(to);
        if (recipientWriter != null) {
            // Envía el mensaje de voz al destinatario
            recipientWriter.println("PRIVATE_VOICE " + voiceData);
        } else {
            PrintWriter senderWriter = users.get(from);
            if (senderWriter != null) {
                // Informar al remitente si el destinatario no existe
                senderWriter.println("Error: User " + to + " not found.");
            }
        }
    }

    // Método para llamadas de voz privadas
    public void callUser(String from, String to) {
        PrintWriter recipientWriter = users.get(to);
        if (recipientWriter != null) {
            // Enviar mensaje de llamada al destinatario
            recipientWriter.println("CALL " + from);
        } else {
            PrintWriter senderWriter = users.get(from);
            if (senderWriter != null) {
                // Informar al remitente si el destinatario no existe
                senderWriter.println("Error: User " + to + " not found.");
            }
        }
    }

    // Método para aceptar llamadas de voz
    public void acceptCall(String from, String to) {
        PrintWriter recipientWriter = users.get(to);
        if (recipientWriter != null) {
            // Enviar mensaje de aceptación al remitente
            recipientWriter.println("CALL_ACCEPTED " + from);
        } else {
            PrintWriter senderWriter = users.get(from);
            if (senderWriter != null) {
                // Informar al remitente si el destinatario no existe
                senderWriter.println("Error: User " + to + " not found.");
            }
        }
    }

    // Método para rechazar llamadas de voz
    public void rejectCall(String from, String to) {
        PrintWriter recipientWriter = users.get(to);
        if (recipientWriter != null) {
            // Enviar mensaje de rechazo al remitente
            recipientWriter.println("CALL_REJECTED " + from);
        } else {
            PrintWriter senderWriter = users.get(from);
            if (senderWriter != null) {
                // Informar al remitente si el destinatario no existe
                senderWriter.println("Error: User " + to + " not found.");
            }
        }
    }

    // Método para enviar el audio de una llamada
    public void sendCallAudio(String from, String to, String audioData) {
        PrintWriter recipientWriter = users.get(to);
        if (recipientWriter != null) {
            // Enviar audio de llamada al destinatario
            recipientWriter.println("CALL_AUDIO " + from + " " + audioData);
        } else {
            PrintWriter senderWriter = users.get(from);
            if (senderWriter != null) {
                // Informar al remitente si el destinatario no existe
                senderWriter.println("Error: User " + to + " not found.");
            }
        }
    }

    // Crear sala de chat
    public void createRoom(String roomName, String creator) {
        chatHistories.put(roomName, new ArrayList<>());
        chatRooms.put(roomName, new HashMap<>());
        addUserToRoom(roomName, creator);
    }

    // Añadir usuario a sala de chat
    public void addUserToRoom(String roomName, String userName) {
        PrintWriter userWriter = users.get(userName);
        if (userWriter != null && chatRooms.containsKey(roomName)) {
            chatRooms.get(roomName).put(userName, userWriter);
        }
    }

    // Enviar mensaje a sala específica
    public void broadcastToRoom(String roomName, String msg) {
        Map<String, PrintWriter> room = chatRooms.get(roomName);
        if (room != null) {
            chatHistories.get(roomName).add(msg);
            for (PrintWriter writer : room.values()) {
                writer.println(msg);
            }
        }
    }

    // Verificar si existe la sala de chat
    public boolean roomExists(String roomName) {
        return chatRooms.containsKey(roomName);
    }

    public Set<String> getRooms() {
        return chatRooms.keySet();
    }

    // Método para obtener el historial de una sala
    public List<String> getRoomHistory(String roomName) {
        return chatHistories.get(roomName);
    }

    // Enviar nota de voz a una sala
    public void sendVoiceMessageToRoom(String roomName, String from, String voiceData) {
        Map<String, PrintWriter> room = chatRooms.get(roomName);
        if (room != null) {
            String voiceMessage = "VOICE_ROOM " + roomName + " " + from + " " + voiceData;
            chatHistories.get(roomName).add(voiceMessage); // Guardar nota de voz en el historial
            for (PrintWriter writer : room.values()) {
                writer.println(voiceMessage);
            }
        } else {
            PrintWriter senderWriter = users.get(from);
            if (senderWriter != null) {
                senderWriter.println("Error: You are not in room " + roomName);
            }
        }
    }

    // Verifica si un usuario está en línea
    public boolean userExists(String username) {
        return users.containsKey(username);
    }
}
