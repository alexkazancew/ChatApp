package chatapplication.alexkazancew.chatapp;

import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import chatapplication.alexkazancew.chatapp.data.Message;
import chatapplication.alexkazancew.chatapp.data.MessageType;

/**
 * Created by alexkazancew on 10.11.16.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int FOREIGN_MESSAGE_TYPE = 0;
    private static final int ACTION_TYPE = 1;
    private static final int MY_MESSAGE_TYPE = 2;

    List<Message> messageList;
   String mMyUserName;


    public ChatAdapter(List<Message> messageList, String myUserName) {
        this.messageList = new ArrayList<>(messageList);
        this.mMyUserName = myUserName;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layout = -1;
        switch (viewType) {
            case FOREIGN_MESSAGE_TYPE:
                layout = R.layout.foreign_message_chat_layoyt_item;
                break;
            case ACTION_TYPE:
                layout = R.layout.action_chat_layout_item;
                break;
            case MY_MESSAGE_TYPE: layout = R.layout.my_message_chat_layout;
                break;
        }


        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);

        return new ViewHolder(v);

    }

    @Override
    public int getItemViewType(int position) {

        Message msg = messageList.get(position);


        if(msg.getType() == MessageType.MESSAGE) {
            if (msg.getUsername().compareTo(mMyUserName) == 0) {
                return MY_MESSAGE_TYPE;
            } else return FOREIGN_MESSAGE_TYPE;
        }
        else if(msg.getType() == MessageType.ACTION)
        {
            return ACTION_TYPE;
        }

        return 0;


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        ((ViewHolder) holder).mMessage.setText(messageList.get(position).getMessage());
        ((ViewHolder) holder).mUserNameTextView.setText(messageList.get(position).getUsername());


    }

    public void addMessage(Message message)
    {
        messageList.add(message);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView mUserNameTextView;
        public AppCompatTextView mMessage;


        public ViewHolder(View itemView) {
            super(itemView);

            mUserNameTextView = (AppCompatTextView) itemView.findViewById(R.id.username_TextView);
            mMessage = (AppCompatTextView) itemView.findViewById(R.id.text_message_TextView);

        }


    }


}
