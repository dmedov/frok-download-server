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
    public static final String TARGET_DIRECTORY = "/Users/den/Documents/syncW7/frok/1";

    private static final String PHOTOS_EXTENSION = ".jpg";
    private static final ObjectMapper MAPPER = new ObjectMapper(); // can reuse, share globally

    private Session session;

    @OnMessage
    public void onMessage(Session session, String msg) {
        try {
            this.session = session;

            if (msg.contains("download_train")) {
                downloadImagesAndLearn(msg);
                clearPhotoFolder((String) MAPPER.readValue(msg, Map.class).get("user_id"));
            } else if (msg.contains("recognize")) {
                recognize(msg);
            } else if (msg.contains("get_faces")) {
                getFaces(msg);
            } else if (msg.contains("save_face")) {
                makeFaceDir((String) MAPPER.readValue(msg, Map.class).get("user_id"));
                Classifier.getInstance().send(msg);
                String recieve = Classifier.getInstance().recieve();
            } else if (msg.contains("train")) {
                List<String> ids = (ArrayList) MAPPER.readValue(msg, Map.class).get("ids");
                clearPhotoFolder(ids.get(0));
                Classifier.getInstance().send(msg);
                String recieve = Classifier.getInstance().recieve();
            } else if (msg.contains("alarm_rec")) {
                Map jsonMap = MAPPER.readValue(msg, Map.class);
                String userId = (String) jsonMap.get("user_id");
                makeFaceDir(userId);
                downloadImageToTargetDir((String)jsonMap.get("link"));
                msg = msg.replace("alarm_rec", "recognize");

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
        downloadImage((String) jsonMap.get("user_id"), (String) jsonMap.get("link"));

        Classifier.getInstance().send(msg);

        String recieve = Classifier.getInstance().recieve();
        Map<String,Object> jsonResult = MAPPER.readValue(recieve, Map.class);

        jsonResult.put("photo_id", jsonMap.get("photo_id"));
        CharArrayWriter w = new CharArrayWriter();
        MAPPER.writeValue(w, jsonResult);

        session.getBasicRemote().sendText(w.toString());
        String success = Classifier.getInstance().recieve();
    }

    private void downloadImagesAndLearn(String msg) throws IOException {
        Map<String,Object> jsonMap = MAPPER.readValue(msg, Map.class);
        try {
            String userId = (String) jsonMap.get("user_id");
            downloadImages(jsonMap);

            // send learn command to classifier
            Classifier.getInstance().send("{\"cmd\":\"train\", \"ids\":[\"" + userId + "\"]}");

            // get response from classifier and send to android

            String recieve = Classifier.getInstance().recieve();
            session.getBasicRemote().sendText(recieve);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadImages(Map<String, Object> jsonMap) throws IOException {
        String userId = (String) jsonMap.get("user_id");

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