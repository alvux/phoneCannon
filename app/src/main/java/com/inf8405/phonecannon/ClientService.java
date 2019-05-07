package com.inf8405.phonecannon;

import android.net.wifi.p2p.WifiP2pInfo;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientService implements CommunicationService {

    private Socket socket;
    private WifiP2pInfo info;
    private CommunicationListener listener;
    private Thread clientThread;
    private Thread commThread;

    public ClientService(WifiP2pInfo info) {
        this.info = info;
    }

    @Override
    public void start() {
        clientThread = new Thread(new ClientThread());
        clientThread.start();
    }

    @Override
    public void register(CommunicationListener listener) {
        this.listener = listener;
    }

    @Override
    public void sendMessage(int type, String message) {
        Log.d("TEST", "Sending message from client: " + message);
        new Thread(() -> {
            try {
                if (null != socket) {
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
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
        if(clientThread != null && clientThread.isAlive()) {
            clientThread.interrupt();
        }
        if (socket != null) {
            if (socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Give up
                    e.printStackTrace();
                }
            }
        }
    }

    class ClientThread implements Runnable {

        public void run() {
            socket = new Socket();
            try {
                Log.d("TEST", "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(info.groupOwnerAddress.getHostName(), 8988)), 5000);
                Log.d("TEST", "Client socket - " + socket.isConnected());

                CommunicationThread communicationThread = new CommunicationThread(socket, new CommunicationListener() {
                    @Override
                    public void onConnection() {
                        if(listener != null) {
                            listener.onConnection();
                        }
                    }

                    @Override
                    public void onNewMessage(int type, String message) {
                        Log.d("TEST", "Got message in client");
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
