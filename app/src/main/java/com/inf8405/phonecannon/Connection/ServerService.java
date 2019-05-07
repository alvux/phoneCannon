package com.inf8405.phonecannon.Connection;

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

    /**
     * Sending messages as strings with the type put at the start like so: type|message
     */
    @Override
    public void sendMessage(int type, String message) {
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

        /**
         * Wait for a client socket to connect and start a communication thread with it
         * Since the game is 1vs1, there's only one client, no need to wait for more connections after
         * the first one
         */
        public void run() {
            serverSocket = null;
            try {
                serverSocket = new ServerSocket(8988);

                // Blocks until connection with a client
                clientSocket = serverSocket.accept();

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
