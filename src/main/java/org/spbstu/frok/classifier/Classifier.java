package org.spbstu.frok.classifier;

import java.io.*;
import java.net.Socket;

public class Classifier
{
    private static final String  IP   = "192.168.1.105";
    private static final Integer PORT = 27015;

    private static Classifier INSTANCE = new Classifier();
    private Socket socket;

    private Classifier() {}

    public static Classifier getInstance() {
        return INSTANCE;
    }

    private void connect() throws IOException {
        socket = new Socket(IP, PORT);
    }

    public void send(String data) throws IOException {
        if (socket == null) {
            connect();
        }

        OutputStream out = socket.getOutputStream();
        out.write(data.getBytes());
    }

    public String recieve() {
        if (socket == null) {
            return null;
        }

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String response;

            while ((response = in.readLine()) != null) {
                return response;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

