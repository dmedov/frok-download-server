package org.spbstu.frok.conn;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.spbstu.frok.classifier.Classifier;
import org.spbstu.frok.settings.Settings;

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

    //public static final String UPLOAD_DIRECTORY = "/home/zda/faces";
    //public static final String TARGET_DIRECTORY = "/home/zda/faces";

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
                String receive = Classifier.getInstance().receive();
            } else if (msg.contains("train")) {
                List<String> ids = (ArrayList) MAPPER.readValue(msg, Map.class).get("userIds");
                clearPhotoFolder(ids.get(0));
                Classifier.getInstance().send(msg);
                String receive = Classifier.getInstance().receive();
            } else if (msg.contains("alarm_rec")) {
                Map jsonMap = MAPPER.readValue(msg, Map.class);
                String userId = (String) jsonMap.get("userId");
                makeFaceDir(userId);
                downloadImageToTargetDir((String)jsonMap.get("phLink"));
                msg = msg.replace("alarm_rec", "recognize");

                try {
                    Classifier.getInstance().send(msg);
                    String receive = Classifier.getInstance().receive();
                    session.getBasicRemote().sendText(receive);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            try {
                session.getBasicRemote().sendText("error : can't connect to classifier");
                return;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void getFaces(String msg) throws IOException {
        Map<String,Object> jsonMap = MAPPER.readValue(msg, Map.class);
        downloadImage((String) jsonMap.get("userId"), (String) jsonMap.get("phLink"));

        Classifier.getInstance().send(msg);

        String receive = Classifier.getInstance().receive();
        Map<String,Object> jsonResult = MAPPER.readValue(receive, Map.class);

        jsonResult.put("phName", jsonMap.get("phName"));
        CharArrayWriter w = new CharArrayWriter();
        MAPPER.writeValue(w, jsonResult);

        session.getBasicRemote().sendText(w.toString());
        String success = Classifier.getInstance().receive();
    }

    private void downloadImagesAndLearn(String msg) throws IOException {
        Map<String,Object> jsonMap = MAPPER.readValue(msg, Map.class);
        try {
            String userId = (String) jsonMap.get("userId");
            downloadImages(jsonMap);

            // send learn command to classifier
            Classifier.getInstance().send("{\"cmd\":\"train\", \"userIds\":[\"" + userId + "\"]}");

            // get response from classifier and send to android

            String receive = Classifier.getInstance().receive();
            session.getBasicRemote().sendText(receive);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadImages(Map<String, Object> jsonMap) throws IOException {
        String userId = (String) jsonMap.get("userId");

        List<String> photoLinks = (ArrayList) jsonMap.get("phLinks");
        if (photoLinks != null) {
            for (String link : photoLinks) {
                // parse photo_id from link
                downloadImage(userId, link);
            }

            makeFaceDir(userId);
        }
    }

    private void makeFaceDir(String userId) {
        File faceDir = new File( Settings.getInstance().getPhotoBasePath() + File.separator +
                                 userId + File.separator +
                                 "faces");
        if (!faceDir.exists()) {
            faceDir.mkdir();
        }
    }

    private void clearPhotoFolder(String userId) throws IOException {
        File photoFolder = new File( Settings.getInstance().getPhotoBasePath() + File.separator +
                                   userId + File.separator + "photos");

        if (photoFolder.exists()) {
            FileUtils.cleanDirectory(photoFolder);
        }
    }

    private void downloadImage(String userId, String link) throws IOException {
        String photoId = link.substring(link.indexOf("?") + 9, link.indexOf("&"));
        // save file by url
        File imageFile = new File( Settings.getInstance().getPhotoBasePath() + File.separator +
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
        File imageFile = new File( Settings.getInstance().getTargetPhotosPath() + File.separator + photoId + PHOTOS_EXTENSION);
        if (!imageFile.exists()) {
            FileUtils.copyURLToFile(new URL(link), imageFile);
        }
    }

    private void recognize(String msg) {
        try {
            Classifier.getInstance().send(msg);

            String receive = Classifier.getInstance().receive();
            session.getBasicRemote().sendText(receive);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}