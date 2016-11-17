package chatapplication.alexkazancew.chatapp;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

import chatapplication.alexkazancew.chatapp.data.Message;
import chatapplication.alexkazancew.chatapp.data.MessageType;
import chatapplication.alexkazancew.chatapp.data.User;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.R.attr.id;
import static android.R.attr.track;

/**
 * Created by alexkazancew on 12.11.16.
 */

public class Communication {

    private static final String SOCKET_IO_TAG = "socket_io";

    private static Communication INSTANCE = null;
    private ChatViewCallback mCallback = null;
    private ChatActivity mChatActivity = null;


    List<PeerClient> mPeerClients;

    boolean isFirstConnection = true;

    Socket mSocket;

    public static Communication getInstance(ChatActivity context, ChatViewCallback callback) {
        if (INSTANCE == null)
            INSTANCE = new Communication(context, callback);

        return INSTANCE;

    }


    public Communication(ChatActivity chatActivity, ChatViewCallback callback) {

        mCallback = callback;
        mChatActivity = chatActivity;
        mPeerClients = new ArrayList<>();



        try {
            IO.Options options1 = new IO.Options();
            options1.hostname = "tz.teleport.media";
            options1.transports = new String[]{"websocket"};
            mSocket = IO.socket("http://tz.teleport.media", options1);
            Log.d(SOCKET_IO_TAG, "SOCKET ID" + mSocket.id());


            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on("login", onLoginSucces);
            mSocket.on("leave", onUserDisconnect);
            mSocket.on("new", onNewUser);
            mSocket.on("offer", onOfferRecive);
            mSocket.on("answer", onAnswerRecive);
            mSocket.on("candidate", onCandidateRecive);
            mSocket.connect();

        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            Log.e(SOCKET_IO_TAG, "error onUriSyntaxException" + ex.getMessage());
        }


    }


    public void sendMessage(String message) {
        for (PeerClient peer : mPeerClients) {
            if(peer.dataChannel !=null)
            peer.dataChannel.send(new DataChannel.Buffer(ByteBuffer.wrap(message.getBytes()), false));
        }
    }



    public void onStop()
    {
        mSocket.emit("disconnect");
        mSocket.disconnect();
        mSocket.close();
    }


    private Emitter.Listener onCandidateRecive = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(SOCKET_IO_TAG, args[0].toString());


            try {

                JSONObject data = (JSONObject) args[0];
                String id = data.getString("id");
                JSONObject candidate = data.getJSONObject("candidate");
                String sdp = candidate.getString("sdp");
                String sdpMid = candidate.getString("sdpMid");
                int sdpMLineIndex = candidate.getInt("sdpMLineIndex");

                PeerClient peer = getPeerClientBySocketId(id);
                peer.peerConnection.addIceCandidate(
                        new IceCandidate(sdpMid, sdpMLineIndex, sdp));
                peer.createDataChanel();


            } catch (Exception ex) {
                Log.e(SOCKET_IO_TAG, "Error onCadidateRecive" + ex.getMessage());
            }
        }
    };

    private Emitter.Listener onAnswerRecive = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(SOCKET_IO_TAG, args[0].toString());


            try {


                JSONObject data = (JSONObject) args[0];
                Log.d(SOCKET_IO_TAG, "ANSWER:\n " + data.toString() );

                final String id = data.getString("id");
                JSONObject discription = data.getJSONObject("offer");
                SessionDescription.Type type = (SessionDescription.Type) discription.get("type");
                String description = data.getString("description");
                getPeerClientBySocketId(id).setRemoteDescr(new SessionDescription(type, description));


            } catch (Exception ex) {
                Log.e(SOCKET_IO_TAG, "Error onAnswerRecive"+ ex.getMessage());
            }


        }
    };

    private Emitter.Listener onOfferRecive = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            try {


                final JSONObject data = (JSONObject) args[0];
                Log.d(SOCKET_IO_TAG, "offer recive:   " + data.toString() );
                final String id = data.getString("id");
                JSONObject discription = data.getJSONObject("offer");
                SessionDescription.Type type = (SessionDescription.Type) discription.get("type");
                String description = data.getString("description");


                User user = new User(id, null);
                PeerClient peerClient = new PeerClient(user);
                mPeerClients.add(peerClient);
                mCallback.newUserConnect(user);


                peerClient.sendAnswer(new SessionDescription(type, description));


            } catch (Exception ex) {
                Log.e(SOCKET_IO_TAG, "Error onOfferRecive "+ ex.getMessage());
            }

        }
    };


    private Emitter.Listener onUserDisconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(SOCKET_IO_TAG, args[0].toString());


            mChatActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];

                    try {
                        Log.d(SOCKET_IO_TAG, "OnDisconnect:  "  + jsonObject.toString());
                        String id = jsonObject.getString("id");
                        mCallback.onUserDisconnect(id);
                        mPeerClients.remove(getPeerClientBySocketId(id));


                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Log.e(SOCKET_IO_TAG, "Error onUserDisconnect parsing"+ ex.getMessage());
                    }

                }
            });


        }
    };


    private Emitter.Listener onNewUser = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(SOCKET_IO_TAG, args[0].toString());
            JSONObject jsonObject = (JSONObject) args[0];
            try {
                final String id = jsonObject.getString("id");
                final String name = jsonObject.getString("name");


                mChatActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        if( getPeerClientBySocketId(id) == null)
                        {

                            mCallback.newUserConnect(new User(id, name));

                            PeerClient peer = new PeerClient(new User(id, name));
                            peer.sendOffer();
                            mPeerClients.add(peer);
                        }

                    }
                });


            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e(SOCKET_IO_TAG, "Erorr parsing on new User"+ ex.getMessage());
            }
        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
//        @Override
        public void call(Object... args) {
            Log.d(SOCKET_IO_TAG, "OPEN!!!!");
//                    socket.send("login", object.toString());
            try {

                if(isFirstConnection) {
                    JSONObject dataClient = new JSONObject();
                    dataClient.put("name", ChatActivity.mMyNickname);
                    mSocket.emit("login", dataClient.toString());
                    isFirstConnection = false;
                }

            } catch (Exception ex) {
                Log.e(SOCKET_IO_TAG, "error parsing on Connect"+ ex.getMessage());
            }

        }
    };


    private Emitter.Listener onLoginSucces = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(SOCKET_IO_TAG, args[0].toString());


            mChatActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    JSONObject jsonObject = (JSONObject) args[0];

                    try {
                        boolean loginSuccess = jsonObject.getBoolean("success");
                        if (loginSuccess) {
                            mCallback.setLoginSucces();
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Log.e(SOCKET_IO_TAG, "Error onLoginSucces parsing"+ ex.getMessage());
                    }

                }
            });
        }
    };


    private PeerClient getPeerClientBySocketId(String id) {
        for (PeerClient peer : mPeerClients) {
            if (peer.getUser().getSocketId().compareTo(id) == 0)
                return peer;
        }

        return null;
    }


    protected class PeerClient {
        User user;
        PeerConnection peerConnection;
        DataChannel dataChannel;

        public PeerClient(final User user) {
            this.user = user;


            List<PeerConnection.IceServer> list = new ArrayList<>();


            PeerConnectionFactory.initializeAndroidGlobals(mChatActivity, true, true, true, null);
            PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

            PeerConnectionFactory peerConnectionFactory = new PeerConnectionFactory();


            peerConnection = peerConnectionFactory.createPeerConnection(list,
                    new MediaConstraints(), new PeerConnection.Observer() {
                        @Override
                        public void onSignalingChange(PeerConnection.SignalingState signalingState) {

                        }

                        @Override
                        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

                        }

                        @Override
                        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

                        }

                        @Override
                        public void onIceCandidate(IceCandidate iceCandidate) {
                            Log.d(SOCKET_IO_TAG, iceCandidate.toString());


                            try {

                                JSONObject candidate = new JSONObject();
                                candidate.put("sdp", iceCandidate.sdp);
                                candidate.put("sdpMid", iceCandidate.sdpMid);
                                candidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                                JSONObject data = new JSONObject();
                                data.put("id", user.getSocketId());
                                data.put("candidate", candidate);

                                mSocket.emit("candidate", data);

                                createDataChanel();


                            } catch (JSONException ex) {
                                Log.d(SOCKET_IO_TAG, "onIceCandidate");
                            }
                        }

                        @Override
                        public void onAddStream(MediaStream mediaStream) {

                        }

                        @Override
                        public void onRemoveStream(MediaStream mediaStream) {

                        }

                        @Override
                        public void onDataChannel(DataChannel dataChannel) {

                        }

                        @Override
                        public void onRenegotiationNeeded() {

                        }
                    });


        }


        public void createDataChanel()
        {
            dataChannel = peerConnection.createDataChannel(user.getName(), new DataChannel.Init());
            dataChannel.registerObserver(new DataChannel.Observer() {
                @Override
                public void onStateChange() {

                }

                @Override
                public void onMessage(DataChannel.Buffer buffer) {
                    mCallback.newMessage(new Message("",
                            new String(buffer.data.asReadOnlyBuffer().array()), MessageType.MESSAGE));
                }
            });

        }

        public void setRemoteDescr(SessionDescription sessionDescription) {
            peerConnection.setRemoteDescription(new SdpObserver() {
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {




                }

                @Override
                public void onSetSuccess() {

                }

                @Override
                public void onCreateFailure(String s) {

                }

                @Override
                public void onSetFailure(String s) {

                }
            }, sessionDescription);
        }

        public void sendOffer() {
            peerConnection.createOffer(new SdpObserver() {
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    try {
                        JSONObject discription = new JSONObject();
                        discription.put("type", sessionDescription.type);
                        discription.put("description", sessionDescription.description);

                        JSONObject data = new JSONObject();
                        data.put("offer", discription);
                        data.put("id", user.getSocketId());

                        Log.d(SOCKET_IO_TAG, "JSON OFFER :   " + data.toString());
                        mSocket.emit("offer", data);

                    } catch (Exception ex) {
                        Log.e(SOCKET_IO_TAG, "error create offer");
                    }


                }

                @Override
                public void onSetSuccess() {

                }

                @Override
                public void onCreateFailure(String s) {

                }

                @Override
                public void onSetFailure(String s) {

                }
            }, new MediaConstraints());


        }


        public void sendAnswer(SessionDescription sessionDescription) {
            peerConnection.setRemoteDescription(new SdpObserver() {
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    peerConnection.createAnswer(new SdpObserver() {
                        @Override
                        public void onCreateSuccess(SessionDescription sessionDescription) {

                            peerConnection.setLocalDescription(new SdpObserver() {
                                @Override
                                public void onCreateSuccess(SessionDescription sessionDescription) {

                                }

                                @Override
                                public void onSetSuccess() {

                                }

                                @Override
                                public void onCreateFailure(String s) {

                                }

                                @Override
                                public void onSetFailure(String s) {

                                }
                            }, sessionDescription);


                            try {
                                JSONObject discription = new JSONObject();
                                discription.put("type", sessionDescription.type);
                                discription.put("description", sessionDescription.description);

                                JSONObject data = new JSONObject();
                                data.put("data", discription);
                                data.put("id", user.getSocketId());

                                mSocket.emit("answer", data);


                            } catch (JSONException ex) {

                            }


                        }

                        @Override
                        public void onSetSuccess() {

                        }

                        @Override
                        public void onCreateFailure(String s) {

                        }

                        @Override
                        public void onSetFailure(String s) {

                        }
                    }, new MediaConstraints());
                }

                @Override
                public void onSetSuccess() {

                }

                @Override
                public void onCreateFailure(String s) {

                }

                @Override
                public void onSetFailure(String s) {

                }
            }, sessionDescription);
        }


        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

    }


}