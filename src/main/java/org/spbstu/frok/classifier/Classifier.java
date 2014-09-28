package org.spbstu.frok.classifier;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.JSONPObject;
import org.spbstu.frok.config.Config;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class Classifier {
    private static final ObjectMapper MAPPER = new ObjectMapper(); // can reuse, share globally
    private static Classifier INSTANCE;

    private static List<ClassifierConnector> classifiers;

    private static HashMap<String, String> responses = new HashMap<>();

    // Synchronize methods
    private static Semaphore sema;
    Integer reqId = new Integer(0);

    private Classifier() {}

    public static Classifier getInstance() {
        synchronized (INSTANCE) {
            if (INSTANCE == null) {
                INSTANCE = new Classifier();
                String classifiersParams = Config.getInstance().getParamValue(Config.CLASSIFIER_ADDRESS_PARAM);
                String[] addresses = classifiersParams.split(",");
                classifiers = new ArrayList<>();

                for (int i = 0; i < addresses.length; ++i) {
                    // [tbd] parse params and fill array list
                    String[] addrAndPort = addresses[i].split(":");
                    classifiers.add(new ClassifierConnector(addrAndPort[0], Integer.parseInt(addrAndPort[1]), INSTANCE));
                }

                sema = new Semaphore(addresses.length, true);
            }
        }
        return INSTANCE;
    }

    public String executeRequest(String request) {
        if(classifiers.isEmpty()) {
            return "{\"result\": \"fail\", \"reason\": \"No classifiers to connect. Something went totally wrong\"}";
        }
        // Wait for ANY of classifiers is free
        try {
            sema.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return  "{\"result\": \"fail\", \"reason\": \"Internal error\"}";
        }

        // Ok, at least one of classifier is free. Now lets search which one is
        int i;
        while(true) {
            for (i = 0; i < classifiers.size(); ++i) {
                if (classifiers.get(i).obtain() == false)
                    continue;
            }
            if (i == classifiers.size()) {
                continue;
            }
            break;
        }

        // Obtained classifier #i
        String requestResult = null;
        ClassifierConnector classifier = classifiers.get(i);

        try {
            // Add "reqId": "###" to json
            StringBuilder stringBuilder = new StringBuilder(request);
            StringBuilder reqIdStringBuilder = new StringBuilder();
            String reqIdStr = null;
            synchronized (reqId) {
                reqIdStr = String.valueOf(reqId++);
            }
            reqIdStringBuilder.append("\"reqId\": \"").append(reqIdStr).append("\",");
            stringBuilder.insert(stringBuilder.indexOf("{"), reqIdStringBuilder.toString());

            // Send request and wait for result (It could be a timeout)
            classifier.send(stringBuilder.toString());
            classifier.receive();

            // Search for the result in hashmap
            if(null == (requestResult = responses.get(reqIdStr))) {
                classifier.release();
                sema.release();
                return "{\"result\": \"fail\", \"reason\": \"Request timeout\"}";
            }
            responses.remove(reqIdStr);
        } catch (IOException e) {
            e.printStackTrace();
            classifier.release();
            sema.release();
            return "{\"result\": \"fail\", \"reason\": \"Internal error\"}";
        }

        // Execute succeed - release obtained resources and return result
        classifier.release();
        sema.release();
        return requestResult;
    }

    public static void addResponse(String response) {
        String requestId = null;
        try {
            requestId = (String) MAPPER.readValue(response, Map.class).get("reqId");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if(requestId != null) {
            responses.put(requestId, response);
        }
    }
}

