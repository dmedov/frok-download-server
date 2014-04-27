package org.spbstu.frok.conn;

import org.spbstu.frok.classifier.Classifier;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.*;

@ServerEndpoint("/main")
public class MainEndPoint {
    @OnMessage
    public void onMessage(Session session, String msg) {
        try {
            Classifier.getInstance().send("test");
        } catch (IOException e) {
            try {
                session.getBasicRemote().sendText("error : cant't connect to classifier");
                return;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        try {
            session.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}