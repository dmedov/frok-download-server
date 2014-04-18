package org.spbstu.frok.conn;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.spbstu.frok.classifier.Classifier;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

@ServerEndpoint("/main")
public class MainEndPoint {

    @OnMessage
    public void onMessage(Session session, String msg) {
        try {
            String userId = "100";

            //String message = msg;

//            if (msg.contains("{")) {
//                JSONObject jsonObject = null;
//                try {
//                    jsonObject = (JSONObject) new JSONParser().parse(message);
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }

                //userId = (String) jsonObject.get("user_id");

                // parse photo links
//            JSONArray photos = (JSONArray) jsonObject.get("photos");
//            Iterator<String> iterator = photos.iterator();
//            while (iterator.hasNext()) {
//                String next = iterator.next();
//                String newFilename = next.substring(next.indexOf("?") + 9, next.indexOf("&"));
//                // save file by url
//                FileUtils.copyURLToFile(new URL(next), new File( Classifier.getUsersFolderPath() +
//                                                                 userId +
//                                                                 File.separator +
//                                                                 newFilename +
//                                                                 Classifier.getImageType()));
//            }

                //  }


//            }

            // classifier saves results in result.json, full path in resultJsonPath
            //runClassifier();

            // open an result.json file, save content of file in string, and send it throught webSockets
            String resultJson = new String(Files.readAllBytes(Paths.get(Classifier.getResultFilename())));
            session.getBasicRemote().sendText(resultJson);//resultJson);

        } catch (IOException e) {
        }
    }

    private void runClassifier() throws IOException {
        Classifier.getInstance().faceDetection(null);
        //Classifier.getInstance().learn();
        Classifier.getInstance().recognize();
    }
}