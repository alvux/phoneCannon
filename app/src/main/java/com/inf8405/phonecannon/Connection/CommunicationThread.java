package com.inf8405.phonecannon.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class CommunicationThread implements Runnable {

        private Socket clientSocket;
        private CommunicationListener listener;
        private BufferedReader input;

        public CommunicationThread(Socket clientSocket, CommunicationListener listener) {
            this.clientSocket = clientSocket;
            this.listener = listener;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                listener.onConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            // While the thread is running, read new messages and notify the listener
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    if (null == read) {
                        Thread.interrupted();
                        read = "Client Disconnected";
                        break;
                    }

                    // Type and message are separated by the char |
                    int indexSeparator = read.indexOf("|");
                    if(indexSeparator > 0) {
                        try {
                            int type = Integer.parseInt(read.substring(0, indexSeparator));
                            if(indexSeparator + 1 < read.length()) {
                                listener.onNewMessage(type, read.substring(indexSeparator + 1));
                            } else {
                                listener.onNewMessage(type, "");
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

}
