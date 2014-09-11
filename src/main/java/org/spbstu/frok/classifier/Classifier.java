package org.spbstu.frok.classifier;

import java.io.*;
import java.net.Socket;

public class Classifier
{
    private static final String  IP   = "127.0.0.1";
    private static final Integer PORT = 27015;

    private static Classifier INSTANCE = new Classifier();
    private Socket socket;

    private BufferedReader socketInputStream;
    private OutputStream socketOutputStream;

    private Classifier() {}

    public static Classifier getInstance() {
        return INSTANCE;
    }

    private void connect() throws IOException {
        socket = new Socket(IP, PORT);

        socketInputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        socketOutputStream = socket.getOutputStream();
    }

    public void send(String data) throws IOException {
        if (socket == null || !socket.isConnected() || socket.isClosed()) {
            clearSocket();
            connect();
        }

        socketOutputStream.write(data.getBytes());
    }

    private void clearSocket() throws IOException {
        if (socket != null) {
            socket.close();
            socketOutputStream.close();
            socketInputStream.close();
        }
    }

    public String recieve() throws IOException {
        if (socket == null || !socket.isConnected() || socket.isClosed()) {
            clearSocket();
            connect();
            return null;
        }

        try {
            String response;

            while ((response = socketInputStream.readLine()) != null) {
                return response;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        clearSocket();
        return null;
    }
}

