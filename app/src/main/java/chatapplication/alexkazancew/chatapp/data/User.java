package chatapplication.alexkazancew.chatapp.data;

/**
 * Created by alexkazancew on 14.11.16.
 */

public class User {

    String socketId;
    String name;

    public User(String  socketId, String name) {
        this.socketId = socketId;
        this.name = name;
    }

    public String getSocketId() {
        return socketId;
    }

    public void setSocketId(String socketId) {
        this.socketId = socketId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
