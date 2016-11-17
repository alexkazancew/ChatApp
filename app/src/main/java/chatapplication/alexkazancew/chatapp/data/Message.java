package chatapplication.alexkazancew.chatapp.data;

/**
 * Created by alexkazancew on 10.11.16.
 */

public class Message {

    String username;
    String message;
    MessageType type;

    public Message(String username, String message, MessageType type) {
        this.username = username;
        this.message = message;
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
