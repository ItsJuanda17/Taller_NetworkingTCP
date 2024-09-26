import java.io.PrintWriter;
import java.net.Socket;
import java.io.IOException;

public abstract class Person {

    public String userName;
    public Socket socket;
    public PrintWriter out;

    public Person(String userName, Socket socket) throws IOException {
        this.userName = userName;
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true); // Auto-flush enabled
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public void sendMessage(String message) {
        out.println(this.userName + ": " + message);
    }


    public void closeConnection() throws IOException {
        out.close();
        socket.close();
    }
}
