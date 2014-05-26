package org.spbstu.frok.conn;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.spbstu.frok.classifier.Classifier;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ServerEndpoint("/main")
public class MainEndPoint {
    public static final String UPLOAD_DIRECTORY = "/Users/den/Documents/syncW7/frok";

    private static final ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
    private static final String PHOTOS_EXTENSION = ".jpg";

    private Session session;

    @OnMessage
    public void onMessage(Session session, String msg) {
        try {
            this.session = session;

            if (msg.contains("download_train")) {
                downloadImagesAndLearn(msg);
            } else if (msg.contains("recognize")) {
                recognize(msg);
            } else if (msg.contains("get_faces")) {

                Map<String,Object> jsonMap = mapper.readValue(msg, Map.class);
                downloadImage((String)jsonMap.get("user_id"), (String)jsonMap.get("link"));

                Classifier.getInstance().send(msg);
                String recieve = Classifier.getInstance().recieve();
                session.getBasicRemote().sendText(recieve);
            }
        } catch (IOException e) {
            try {
                session.getBasicRemote().sendText("error : cant't connect to classifier");
                return;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void downloadImagesAndLearn(String msg) throws IOException {
        Map<String,Object> jsonMap = mapper.readValue(msg, Map.class);
        try {
            String userId = downloadImages(jsonMap);

            // send learn command to classifier
            Classifier.getInstance().send("{\"cmd\":\"train\", \"ids\":[\"" + userId + "\"]}");

            // get response from classifier and send to android

            String recieve = Classifier.getInstance().recieve();
            session.getBasicRemote().sendText(recieve);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String downloadImages(Map<String, Object> jsonMap) throws IOException {
        String userId = (String) jsonMap.get("user_id");

        List<String> photoLinks = (ArrayList) jsonMap.get("photos");
        if (photoLinks != null) {
            for (String link : photoLinks) {
                // parse photo_id from link
                downloadImage(userId, link);
            }

            File faceDir = new File( UPLOAD_DIRECTORY + File.separator +
                                     userId + File.separator +
                                     "faces");
            faceDir.mkdir();
        }
        return userId;
    }

    private void downloadImage(String userId, String link) throws IOException {
        String photoId = link.substring(link.indexOf("?") + 9, link.indexOf("&"));
        // save file by url
        File imageFile = new File( UPLOAD_DIRECTORY + File.separator +
                                   userId + File.separator +
                                   "photos" + File.separator +
                                   photoId + PHOTOS_EXTENSION);
        if (!imageFile.exists()) {
            FileUtils.copyURLToFile(new URL(link), imageFile);
        }
    }

    private void recognize(String msg) {
        try {
            Classifier.getInstance().send(msg);

            String recieve = Classifier.getInstance().recieve();
            session.getBasicRemote().sendText(recieve);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}