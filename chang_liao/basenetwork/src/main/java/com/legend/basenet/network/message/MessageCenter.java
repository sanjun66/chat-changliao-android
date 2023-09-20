package com.legend.basenet.network.message;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.legend.basenet.network.NetworkManager;
import com.legend.basenet.network.listener.NetworkListener;

public class MessageCenter{

    private static final String TAG = MessageCenter.class.getSimpleName();

    public static final int WHAT_TOKEN_EXPIRED = 1;
    public static final int WHAT_RESPONSE_CMD = 2;

    private static volatile MessageCenter instance = null;
    private final Handler handler;

    public MessageCenter() {
        handler = new MessageHandler(Looper.getMainLooper());
    }

    public static MessageCenter getInstance() {
        if (instance == null) {
            synchronized (MessageCenter.class) {
                if (instance == null) {
                    instance = new MessageCenter();
                }
            }
        }
        return instance;
    }

    public static void sendTokenExpired(int code) {
        Message message = new Message();
        message.what = WHAT_TOKEN_EXPIRED;
        message.obj = code;
        sendMessage(message);
    }

    public static void sendCmd(String cmd) {
        Message message = new Message();
        message.what = WHAT_RESPONSE_CMD;
        message.obj = cmd;
        sendMessage(message);
    }

    private static void sendMessage(Message message) {
        MessageCenter.getInstance().handler.sendMessage(message);
    }

    public static class MessageHandler extends Handler {

        public MessageHandler(Looper mainLooper) {
            super(mainLooper);
        }

        @Override
        public void handleMessage(Message msg) {

            NetworkListener listener = NetworkManager.getInstance().getNetworkListener();

            if (msg.what == WHAT_TOKEN_EXPIRED) {
                if (listener != null) {
                    listener.onTokenExpired((Integer) msg.obj);
                }
            } else if (msg.what == WHAT_RESPONSE_CMD) {
                if (listener != null) {
                    listener.onReceivedCmd((String) msg.obj);
                }
            }
        }
    }
}
