package org.spbstu.frok.classifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Semaphore;

/**
 * Created by zda on 28.09.14.
 */
public class ClassifierConnector {
    private String  ip;
    private Integer port;

    private Socket socket;
    private BufferedReader socketInputStream;
    private OutputStream socketOutputStream;

    protected ClassifierConnector(String ipv4Address, Integer port) {
        this.ip = ipv4Address;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket(ip, port);

        socketInputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        socketOutputStream = socket.getOutputStream();
    }

    public void send(String data) throws IOException {
        //if (socket == null || !socket.isConnected() || socket.isClosed()) {
            refreshConnection();
        //}
        try {
            socketOutputStream.write(data.getBytes());
        } catch (IOException e) {
            refreshConnection();
            socketOutputStream.write(data.getBytes());
        }
    }

    public void refreshConnection() throws IOException {
        if(socket == null) {
            socket = new Socket(ip, port);
        }
        clearSocket();
        connect();
    }

    public String receive() throws IOException {
        if (socket == null || !socket.isConnected() || socket.isClosed()) {
            return null;
        }

        String response = null;
        try {
            response = socketInputStream.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private void clearSocket() throws IOException {
        if (socket != null) {
            socket.close();
            socketOutputStream.close();
            socketInputStream.close();
        }
    }
}
