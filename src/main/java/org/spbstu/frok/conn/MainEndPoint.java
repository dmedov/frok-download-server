package org.spbstu.frok.conn;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.spbstu.frok.classifier.Classifier;
import org.spbstu.frok.config.Config;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@ServerEndpoint("/main")
public class MainEndPoint {
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
                String receive = Classifier.getInstance().executeRequest(msg);
                session.getBasicRemote().sendText(receive);
            } else if (msg.contains("train")) {
                List<String> ids = (ArrayList) MAPPER.readValue(msg, Map.class).get("userIds");
                clearPhotoFolder(ids.get(0));
                String receive = Classifier.getInstance().executeRequest(msg);
                session.getBasicRemote().sendText(receive);
            } else if (msg.contains("alarm_rec")) {
                Map jsonMap = MAPPER.readValue(msg, Map.class);
                String userId = (String) jsonMap.get("userId");
                makeFaceDir(userId);
                downloadImageToTargetDir((String)jsonMap.get("phLink"));
                msg = msg.replace("alarm_rec", "recognize");

                try {
                    String receive = Classifier.getInstance().executeRequest(msg);
                    session.getBasicRemote().sendText(receive);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            try {
                session.getBasicRemote().sendText("{\"result\" : \"fail\", \"reason\": \"some error occurred\"");
                return;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void getFaces(String msg) throws IOException {
        Map<String,Object> jsonMap = MAPPER.readValue(msg, Map.class);
        downloadImage((String) jsonMap.get("userId"), (String) jsonMap.get("phLink"));

        String receive = Classifier.getInstance().executeRequest(msg);

        // add phName to classifier result
        String phName = ",\"phName\":\"" + jsonMap.get("phName") + "\"";
        receive = new StringBuilder(receive).insert(receive.lastIndexOf("\"") + 1, phName).toString();
        session.getBasicRemote().sendText(receive);
    }

    private void downloadImagesAndLearn(String msg) throws IOException {
        Map<String,Object> jsonMap = MAPPER.readValue(msg, Map.class);
        try {
            String userId = (String) jsonMap.get("userId");
            downloadImages(jsonMap);

            // send learn command to classifier
            // get response from classifier and send to android
            String receive = Classifier.getInstance().executeRequest("{\"cmd\":\"train\", \"userIds\":[\"" + userId + "\"]}");
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
        File faceDir = new File( Config.getInstance().getParamValue(Config.PHOTO_BASE_PATH_PARAM) + File.separator +
                                 userId + File.separator +
                                 "faces");
        if (!faceDir.exists()) {
            faceDir.mkdir();
        }
    }

    private void clearPhotoFolder(String userId) throws IOException {
        File photoFolder = new File( Config.getInstance().getParamValue(Config.PHOTO_BASE_PATH_PARAM) + File.separator +
                                   userId + File.separator + "photos");

        if (photoFolder.exists()) {
            FileUtils.cleanDirectory(photoFolder);
        }
    }

    private void downloadImage(String userId, String link) throws IOException {
        String photoId = link.substring(link.indexOf("?") + 9, link.indexOf("&"));
        // save file by url
        String userDirPath = Config.getInstance().getParamValue(Config.PHOTO_BASE_PATH_PARAM) + File.separator +
                userId;

        String userPhotosDir = userDirPath + File.separator + "photos";
        String userFacesDir  = userDirPath + File.separator + "faces";

        File imageFile = new File( userPhotosDir + File.separator +
                                   photoId + PHOTOS_EXTENSION);

        File faceFile = new File(userFacesDir + File.separator +
                                   photoId + PHOTOS_EXTENSION);

        // check if we try download image which user faces recognized for this image
        // we delete faces file from userFacesDir contains photoId in his names
        File dir = new File(userFacesDir);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File child : files) {
                if (child.getName().contains(photoId)) {
                    child.delete();
                }
            }
        }

        if (!imageFile.exists()) {
            FileUtils.copyURLToFile(new URL(link), imageFile);
        }
    }

    private void downloadImageToTargetDir(String link) throws IOException {
        String photoId = link.substring(link.indexOf("?") + 9, link.indexOf("&"));
        // save file by url
        File imageFile = new File( Config.getInstance().getParamValue(Config.TARGET_PHOTO_PATH_PARAM) + File.separator
                + photoId + PHOTOS_EXTENSION);
        if (!imageFile.exists()) {
            FileUtils.copyURLToFile(new URL(link), imageFile);
        }
    }

    private void recognize(String msg) {
        try {
            String receive = Classifier.getInstance().executeRequest(msg);
            session.getBasicRemote().sendText(receive);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}