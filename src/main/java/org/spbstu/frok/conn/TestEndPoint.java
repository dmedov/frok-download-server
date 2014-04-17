package org.spbstu.frok.conn;

import org.spbstu.frok.classifier.Classifier;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

@ServerEndpoint("/echo")
public class TestEndPoint {
    private final static String resultJsonPath = "/tmp/result.json";

    @OnMessage
    public void onMessage(Session session, String msg) {
        try {
            // classifier saves results in result.json, full path in resultJsonPath

            //runClassifier();

            try {
                Thread.sleep(40000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // open an result.json file, save content of file in string, and send it throught webSockets
            String resultJson = new String(Files.readAllBytes(Paths.get(resultJsonPath)));
            session.getBasicRemote().sendText(resultJson);

        } catch (IOException e) {
        }
    }

    private void runClassifier() throws IOException {
        Classifier.getInstance().faceDetection(null);
        Classifier.getInstance().learn();
        Classifier.getInstance().recognize();
    }
}