package org.spbstu.frok.classifier;

import org.codehaus.jackson.map.ObjectMapper;
import org.spbstu.frok.config.Config;

import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Classifier {
    private static final ObjectMapper MAPPER = new ObjectMapper(); // can reuse, share globally
    private static Classifier INSTANCE = null;

    private static LinkedList<ClassifierConnector> classifiersList = new LinkedList<ClassifierConnector>();
    private static Iterator classifierIterator;

    private final Lock classifierCS = new ReentrantLock(true);
    Integer reqId = new Integer(0);

    private Classifier() {}

    public static Classifier getInstance() {
        if (INSTANCE == null) {
            String classifiersParams = Config.getInstance().getParamValue(Config.CLASSIFIER_ADDRESS_PARAM);
            String[] addresses = classifiersParams.split(",");

            for (int i = 0; i < addresses.length; ++i) {
                // [tbd] parse params and fill array list
                String[] addrAndPort = addresses[i].split(":");
                ClassifierConnector connector = new ClassifierConnector(addrAndPort[0], Integer.parseInt(addrAndPort[1]));
                try {
                    connector.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    classifiersList.add(connector);
                }
            }

            classifierIterator = classifiersList.listIterator();

            INSTANCE = new Classifier();
        }
        return INSTANCE;
    }

    public String executeRequest(String request) throws IOException {
        if(classifiersList.isEmpty()) {
            return "{\"result\": \"fail\", \"reason\": \"No classifiers to connect. Something went totally wrong\"}";
        }

        ClassifierConnector classifier = null;
        String requestResult = null;

        classifierCS.lock();
        if (!classifierIterator.hasNext()) {
            classifierIterator = classifiersList.listIterator();
        }
        classifier = (ClassifierConnector) classifierIterator.next();
        classifierCS.unlock();

        try {
            // Add "reqId": "###" to json
            StringBuilder stringBuilder = new StringBuilder(request);
            StringBuilder reqIdStringBuilder = new StringBuilder();

            classifierCS.lock();
            String reqIdStr = String.valueOf(reqId++);
            classifierCS.unlock();

            reqIdStringBuilder.append("\"reqId\": \"").append(reqIdStr).append("\",");
            stringBuilder.insert(stringBuilder.indexOf("{") + 1, reqIdStringBuilder.toString());

            // Send request and wait for result (It could be a timeout)
            classifier.send(stringBuilder.toString());
            requestResult = classifier.receive();
            if(null == requestResult) {
                classifier.refreshConnection();
                return "{\"result\": \"fail\", \"reason\": \"internal error\"}";
            }
        } catch (IOException e) {
            e.printStackTrace();
            classifier.refreshConnection();
            return "{\"result\": \"fail\", \"reason\": \"Internal error\"}";
        }

        // Execute succeed - release obtained resources and return result
        return requestResult;
    }
}

