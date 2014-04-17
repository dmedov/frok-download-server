package org.spbstu.frok.classifier;

import org.spbstu.frok.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Classifier
{
    private static Classifier INSTANCE = new Classifier();
    private static final String PROCESS_DIRECTORY = "C:\\Face_detector_OK\\";
    private static final String RESULT_FILENAME = "result.json";

    private static final String EXE_PATH = PROCESS_DIRECTORY + "FaceDetectionApp.exe";
    private static final String TMP_FOLDER = PROCESS_DIRECTORY + "tmp\\";

    private Classifier() {}

    public static Classifier getInstance() {
        return INSTANCE;
    }

    public void faceDetection(String userId) {
        Utils.executeCommand(PROCESS_DIRECTORY, new ArrayList<String>() {{
                                                        add(EXE_PATH);
                                                        add(TMP_FOLDER + "5\\");
                                                        add("-f");
        }});
    }

    public void learn() {
        Utils.executeCommand(PROCESS_DIRECTORY, new ArrayList<String>() {{
            add(EXE_PATH);
            add(TMP_FOLDER);
            add("-l");
        }});
    }

    public void recognize() {
        Utils.executeCommand(PROCESS_DIRECTORY, new ArrayList<String>() {{
            add(EXE_PATH);
            add(TMP_FOLDER + "target.jpg");
            add("-r");
            add(TMP_FOLDER);
        }});
    }

    public static String getResultFilename() {
        return RESULT_FILENAME;
    }
}

