package com.inf8405.phonecannon.Connection;

// To be implemented by classes that want to be notified of socket events
public interface CommunicationListener {
    void onConnection();
    void onNewMessage(int type, String message);
}
