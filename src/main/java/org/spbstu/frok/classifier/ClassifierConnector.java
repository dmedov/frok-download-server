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
    private static Integer RESPONSE_TIMEOUT_MS = 120000;
    private static String  ip;
    private static Integer port;

    private static final Classifier callback = Classifier.getInstance();
    private Socket socket;

    private BufferedReader socketInputStream;
    private OutputStream socketOutputStream;

    // Synchronize stuff
    private Semaphore sema = new Semaphore(1, true);

    protected ClassifierConnector(String ipv4Address, Integer port, Classifier callback) {
        this.ip = ipv4Address;
        this.port = port;
    }

    public boolean obtain() {
        return sema.tryAcquire();
    }

    public void release() {
        sema.release();
    }

    private void connect() throws IOException {
        socket = new Socket(ip, port);

        socketInputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        socketOutputStream = socket.getOutputStream();
    }

    public void send(String data) throws IOException {
        if (socket == null || !socket.isConnected() || socket.isClosed()) {
            refreshConnection();
        }
        try {
            socketOutputStream.write(data.getBytes());
        } catch (IOException e) {
            refreshConnection();
            socketOutputStream.write(data.getBytes());
        }
    }

    private void refreshConnection() throws IOException {
        if(socket == null) {
            socket = new Socket(ip, port);
        }
        clearSocket();
        connect();
    }

    private void clearSocket() throws IOException {
        if (socket != null) {
            socket.close();
            socketOutputStream.close();
            socketInputStream.close();
        }
    }

    public boolean receive() throws IOException {
        if (socket == null || !socket.isConnected() || socket.isClosed()) {
            try {
                clearSocket();
                connect();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        Thread socketListenerThread = new Thread(new Runnable() {
            Classifier responseCallback = callback;
            @Override
            public void run() {
                try {

                    String response;

                    if ((response = socketInputStream.readLine()) != null) {
                        responseCallback.addResponse(response);
                    }
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        socketListenerThread.start();
        long endTimeMillis = System.currentTimeMillis() + RESPONSE_TIMEOUT_MS;
        while (socketListenerThread.isAlive()) {
            if (System.currentTimeMillis() > endTimeMillis) {
                System.out.println("Timeout has occurred");
                refreshConnection();
                return false;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException t) {/*do nothing*/}
        }

        return true;
    }
}
