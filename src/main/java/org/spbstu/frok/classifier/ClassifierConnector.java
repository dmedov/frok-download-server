package org.spbstu.frok.classifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClassifierConnector {
    private String  ip;
    private Integer port;

    private Socket socket;
    private BufferedReader socketInputStream = null;
    private OutputStream socketOutputStream = null;

    private static final Lock lockerCS = new ReentrantLock(true);
    private final Lock locker = new ReentrantLock(true);

    protected ClassifierConnector(String ipv4Address, Integer port) {
        this.ip = ipv4Address;
        this.port = port;
    }

    public boolean acquire() {
        lockerCS.lock();
        locker.lock();
        lockerCS.unlock();
        return true;
    }

    public void release() {
        locker.unlock();
    }

    public void connect() throws IOException {
        socket = new Socket(ip, port);

        socketInputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        socketOutputStream = socket.getOutputStream();
    }

    public void send(String data) throws IOException {
        refreshConnection();
        try {
            socketOutputStream.write(data.getBytes());
        } catch (IOException e) {
            refreshConnection();
            socketOutputStream.write(data.getBytes());
        }
    }

    public void refreshConnection() throws IOException {
        clearSocket();
        connect();
    }

    public String receive() throws IOException {
        if (socket == null || !socket.isConnected() || socket.isClosed()) {
            return null;
        }

        return socketInputStream.readLine();
    }

    private void clearSocket() throws IOException {
        if (socket != null) {
            socket.close();
        }
        if (socketOutputStream != null) {
            socketOutputStream.close();
        }
        if(socketInputStream != null) {
            socketInputStream.close();
        }
    }
}
