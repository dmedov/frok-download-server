package org.spbstu.frok.classifier;

import org.spbstu.frok.Utils;

import java.util.ArrayList;

public class Classifier
{
    private static final String PROCESS_DIRECTORY = "C:\\Face_detector_OK\\";
    private static final String RESULT_FILENAME   = "result.json";
    private static final String EXE_PATH          = PROCESS_DIRECTORY + "FaceDetectionApp.exe";
    private static final String USERS_FOLDER      = PROCESS_DIRECTORY + "tmp\\";
    private static final String IMAGE_TYPE        = ".jpg";

    private static Classifier INSTANCE = new Classifier();
    private Classifier() {}
    public static Classifier getInstance() {
        return INSTANCE;
    }

    public void faceDetection(final String userId) {
        Utils.executeCommand(PROCESS_DIRECTORY, new ArrayList<String>() {{
                                                        add(EXE_PATH);
                                                        add(USERS_FOLDER + userId + "\\");
                                                        add("-f");
        }});
    }

    public void learn() {
        Utils.executeCommand(PROCESS_DIRECTORY, new ArrayList<String>() {{
                                                        add(EXE_PATH);
                                                        add(USERS_FOLDER);
                                                        add("-l");
        }});
    }

    public void recognize() {
        Utils.executeCommand(PROCESS_DIRECTORY, new ArrayList<String>() {{
                                                        add(EXE_PATH);
                                                        add(USERS_FOLDER + "target.jpg");
                                                        add("-r");
                                                        add(USERS_FOLDER);
        }});
    }

    public static String getResultFilename() {
        return PROCESS_DIRECTORY + RESULT_FILENAME;
    }

    public static String getUsersFolderPath() {
        return USERS_FOLDER;
    }

    public static String getImageType() {
        return IMAGE_TYPE;
    }
}

