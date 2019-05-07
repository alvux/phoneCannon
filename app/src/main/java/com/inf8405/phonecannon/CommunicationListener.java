package com.inf8405.phonecannon;

public interface CommunicationListener {
    void onConnection();
    void onNewMessage(int type, String message);
}
