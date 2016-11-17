package chatapplication.alexkazancew.chatapp;

import chatapplication.alexkazancew.chatapp.data.Message;
import chatapplication.alexkazancew.chatapp.data.User;

/**
 * Created by alexkazancew on 14.11.16.
 */

public interface ChatViewCallback{

    void setLoginSucces();

    void newUserConnect(User user);

    void onUserDisconnect(String id);

    void newMessage(Message msg);



}
