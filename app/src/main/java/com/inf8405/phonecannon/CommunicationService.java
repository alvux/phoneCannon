package com.inf8405.phonecannon;

public interface CommunicationService {
    void start();
    void register(CommunicationListener listener);
    void sendMessage(int type, String message);
    void disconnect();
}
