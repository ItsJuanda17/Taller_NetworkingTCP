import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Chatters {
    private final Map<String, PrintWriter> users;
    private final Map<String, Map<String, PrintWriter>> chatRooms;

    public Chatters() {
        users = new HashMap<>();
        chatRooms = new HashMap<>();
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

    // Enviar mensaje de voz
    public void sendVoiceMessage(String from, String audioFilePath) {
        for (PrintWriter writer : users.values()) {
            writer.println("VOICE " + from + " " + audioFilePath);
        }
    }

    // Crear sala de chat
    public void createRoom(String roomName) {
        chatRooms.put(roomName, new HashMap<>());
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
}
