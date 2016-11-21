package chatapplication.alexkazancew.chatapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ListViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;


import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.List;

import chatapplication.alexkazancew.chatapp.data.Message;
import chatapplication.alexkazancew.chatapp.data.MessageType;
import chatapplication.alexkazancew.chatapp.data.User;

/**
 * Created by alexkazancew on 10.11.16.
 */

public class ChatActivity extends AppCompatActivity implements ChatViewCallback {

    RecyclerView mRecycler;
    ChatAdapter mAdapter;
    LinearLayoutManager mLinearLayoutManager;

    AppCompatEditText mEditMessage;
    AppCompatImageButton mSendMessageButton;

    DrawerLayout mDrawerLayout;
    ListView mDrawerListView;
    DrawerListAdapter mDrawerListAdapter;

    Communication mComunication;

    ProgressBar mProgressBar;

    public static String mMyNickname;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout_activity);

        mComunication = Communication.getInstance(this, this);


        mMyNickname = getIntent().getStringExtra(LoginActivity.USER_NAME);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_Layoyt);
        mDrawerListView = (ListView) findViewById(R.id.drawer_listView);

        View header = getLayoutInflater().inflate(R.layout.header_view, null);
        mDrawerListView.addHeaderView(header);


        mDrawerListAdapter = new DrawerListAdapter(new ArrayList<User>(0));
        mDrawerListView.setAdapter(mDrawerListAdapter);


        mRecycler = (RecyclerView) findViewById(R.id.recycler_view);
        mEditMessage = (AppCompatEditText) findViewById(R.id.input_message_textEdit);
        mSendMessageButton = (AppCompatImageButton) findViewById(R.id.send_message_button);
        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });


        mAdapter = new ChatAdapter(new ArrayList<Message>(0), mMyNickname);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(mLinearLayoutManager);


    }


    private void sendMessage() {
        String message = mEditMessage.getText().toString();
        if (!message.isEmpty()) {
            mAdapter.addMessage(new Message(mMyNickname, message, MessageType.MESSAGE));
            mComunication.sendMessage(message);
            mEditMessage.setText("");
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        mComunication.onStop();
    }

    protected class DrawerListAdapter extends BaseAdapter {
        List<User> mUsers;


        public DrawerListAdapter(List<User> nicknames) {
            super();
            mUsers = new ArrayList<>(nicknames);
        }

        public void addUser(User user) {
            mUsers.add(user);
            notifyDataSetChanged();
        }

        public void deleteUser(String id) {

            mUsers.remove(getUserById(id));
            notifyDataSetChanged();
        }

        public User getUserById(String id) {
            for (User u : mUsers) {
                if (u.getSocketId().compareTo(id) == 0)
                    return u;
            }
            return null;

        }

        @Override
        public int getCount() {
            return mUsers.size();
        }

        @Override
        public User getItem(int i) {
            return mUsers.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {


            View v = view;

            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.drawer_list_item_layoyt, viewGroup, false);

            ((AppCompatTextView) v.findViewById(R.id.item_drawer_list_textView)).setText(mUsers.get(i).getName());

            return v;

        }
    }


    @Override
    public void setLoginSucces() {

        mProgressBar.setVisibility(View.GONE);
        mRecycler.setVisibility(View.VISIBLE);

        mAdapter.addMessage(new Message("Вы", getString(R.string.you_enter_to_chat), MessageType.ACTION));


    }

    @Override
    public void newUserConnect(User user) {

        if (!mDrawerListAdapter.mUsers.contains(user)) {
            mAdapter.addMessage(new Message(user.getName(), getString(R.string.enter_to_chat), MessageType.ACTION));
            mDrawerListAdapter.addUser(user);
        }
    }

    @Override
    public void onUserDisconnect(String id) {
        mDrawerListAdapter.deleteUser(id);
        User user = mDrawerListAdapter.getUserById(id);

//        mAdapter.addMessage(new Message(user.getName(), getString(R.string.exit_fram_chat), MessageType.ACTION));

    }


    @Override
    public void newMessage(Message msg) {
        mAdapter.addMessage(msg);
    }
}
