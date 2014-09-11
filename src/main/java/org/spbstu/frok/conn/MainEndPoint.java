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
    public static final String UPLOAD_DIRECTORY = "/home/zda/faces";
    public static final String TARGET_DIRECTORY = "/home/zda/faces";

    private static final String PHOTOS_EXTENSION = ".jpg";
    private static final ObjectMapper MAPPER = new ObjectMapper(); // can reuse, share globally

    private Session session;

    @OnMessage
    public void onMessage(Session session, String msg) {
        try {
            this.session = session;

            if (msg.contains("download_train")) {
                downloadImagesAndLearn(msg);
                clearPhotoFolder((String) MAPPER.readValue(msg, Map.class).get("userId"));
            } else if (msg.contains("recognize")) {
                recognize(msg);
            } else if (msg.contains("getFaces")) {
                getFaces(msg);
            } else if (msg.contains("addFace")) {
                makeFaceDir((String) MAPPER.readValue(msg, Map.class).get("userId"));
                Classifier.getInstance().send(msg);
                String recieve = Classifier.getInstance().recieve();
            } else if (msg.contains("train")) {
                List<String> ids = (ArrayList) MAPPER.readValue(msg, Map.class).get("arrUserIds");
                clearPhotoFolder(ids.get(0));
                Classifier.getInstance().send(msg);
                String recieve = Classifier.getInstance().recieve();
            } else if (msg.contains("alarm_rec")) {
                Map jsonMap = MAPPER.readValue(msg, Map.class);
                String userId = (String) jsonMap.get("user_id");
                makeFaceDir(userId);
                downloadImageToTargetDir((String)jsonMap.get("link"));
                msg = msg.replace("alarm_rec", "recognize");
                msg = msg.replace("user_id", "userId");
                msg = msg.replace("photo_id", "photoName");
                try {
                    Classifier.getInstance().send(msg);
                    String recieve = Classifier.getInstance().recieve();
                    session.getBasicRemote().sendText(recieve);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    private void getFaces(String msg) throws IOException {
        Map<String,Object> jsonMap = MAPPER.readValue(msg, Map.class);
        downloadImage((String) jsonMap.get("userId"), (String) jsonMap.get("link"));

        Classifier.getInstance().send(msg);

        String recieve = Classifier.getInstance().recieve();
        Map<String,Object> jsonResult = MAPPER.readValue(recieve, Map.class);

        jsonResult.put("photoName", jsonMap.get("photoName"));
        CharArrayWriter w = new CharArrayWriter();
        MAPPER.writeValue(w, jsonResult);

        session.getBasicRemote().sendText(w.toString());
        String success = Classifier.getInstance().recieve();
    }

    private void downloadImagesAndLearn(String msg) throws IOException {
        Map<String,Object> jsonMap = MAPPER.readValue(msg, Map.class);
        try {
            String userId = (String) jsonMap.get("userId");
            downloadImages(jsonMap);

            // send learn command to classifier
            Classifier.getInstance().send("{\"cmd\":\"train\", \"arrUserIds\":[\"" + userId + "\"]}");

            // get response from classifier and send to android

            String recieve = Classifier.getInstance().recieve();
            session.getBasicRemote().sendText(recieve);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadImages(Map<String, Object> jsonMap) throws IOException {
        String userId = (String) jsonMap.get("userId");

        List<String> photoLinks = (ArrayList) jsonMap.get("photos");
        if (photoLinks != null) {
            for (String link : photoLinks) {
                // parse photo_id from link
                downloadImage(userId, link);
            }

            makeFaceDir(userId);
        }
    }

    private void makeFaceDir(String userId) {
        File faceDir = new File( UPLOAD_DIRECTORY + File.separator +
                                 userId + File.separator +
                                 "faces");
        if (!faceDir.exists()) {
            faceDir.mkdir();
        }
    }

    private void clearPhotoFolder(String userId) throws IOException {
        File photoFolder = new File( UPLOAD_DIRECTORY + File.separator +
                                   userId + File.separator + "photos");

        if (photoFolder.exists()) {
            FileUtils.cleanDirectory(photoFolder);
        }
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

    private void downloadImageToTargetDir(String link) throws IOException {
        String photoId = link.substring(link.indexOf("?") + 9, link.indexOf("&"));
        // save file by url
        File imageFile = new File( UPLOAD_DIRECTORY + File.separator +
                                   "1" + File.separator + photoId + PHOTOS_EXTENSION);
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