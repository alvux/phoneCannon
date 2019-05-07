package com.inf8405.phonecannon;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerService implements CommunicationService {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private CommunicationListener listener;
    private Thread serverThread;
    private Thread commThread;

    @Override
    public void start() {
        serverThread = new Thread(new ServerThread());
        serverThread.start();
    }

    @Override
    public void register(CommunicationListener listener) {
        this.listener = listener;
    }

    @Override
    public void sendMessage(int type, String message) {
        Log.d("TEST", "Sending message from server: " + message);
        new Thread(() -> {
            try {
                if (null != clientSocket) {
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(clientSocket.getOutputStream())),
                            true);
                    out.println(String.valueOf(type) + "|" + message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void disconnect() {
        if(commThread != null && commThread.isAlive()) {
            commThread.interrupt();
        }
        if(serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
        }
        if(serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ServerThread implements Runnable {

        public void run() {
            serverSocket = null;
            try {
                serverSocket = new ServerSocket(8988);
                Log.d("TEST", "Server: Socket opened");

                // Blocks until connection with a client
                clientSocket = serverSocket.accept();
                Log.d("TEST", "Server: connection with client done");

                // Creating the serverThread that will listen for new messages
                CommunicationThread communicationThread = new CommunicationThread(clientSocket, new CommunicationListener() {
                    @Override
                    public void onConnection() {
                        if(listener != null) {
                            listener.onConnection();
                        }
                    }

                    @Override
                    public void onNewMessage(int type, String message) {
                        Log.d("TEST", "Got message in server");
                        if(listener != null) {
                            listener.onNewMessage(type, message);
                        }
                    }
                });
                commThread = new Thread(communicationThread);
                commThread.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
