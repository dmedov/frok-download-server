package org.spbstu.frok.settings;

/**
 * Created by zda on 17.09.14.
 */

import sun.org.mozilla.javascript.ast.NumberLiteral;

import java.io.*;
import java.util.List;

class ClassifierAddress {
    private String  IP;
    private Integer PORT;
    public ClassifierAddress(String ip, Integer port) {
        IP = new String(ip);
        PORT = new Integer(port);
    }
}

public class Settings {
    private static final char pSeparator = '=';
    private static final String pName_photoBasePath = "PHOTO_BASE_PATH";
    private static final String pName_targetPhotosPath = "TARGET_PHOTOS_PATH";
    private static final String pName_classifierAddress = "TARGET_PHOTOS_PATH";
    private String photoBasePath = new String();
    private String targetPhotosPath = new String();
    private List<ClassifierAddress> classifierAddress;

    private static Settings INSTANCE;

    private Settings() {}

    public static Settings getInstance()
    {
        if(INSTANCE == null)
        {
            INSTANCE = new Settings();
        }
        return INSTANCE;
    }

    public boolean parseConfigFile(String fileName)
    {
        File fp = new File(fileName);
        if(fp.canRead()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(fp));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if(line.contains(pName_classifierAddress))
                    {
                        int sIndex = line.indexOf(pSeparator) + 1;
                        while(line.charAt(sIndex) == pSeparator || line.charAt(sIndex) == ' ') {
                            ++sIndex;
                        }
                        String ipAndPort = line.substring(sIndex);
                        // Format is 127.0.0.1:27015
                        sIndex = ipAndPort.indexOf(':');
                        String ip =  ipAndPort.substring(0, sIndex - 1);
                        String port =  ipAndPort.substring(0, sIndex + 1);
                        classifierAddress.add(new ClassifierAddress(ip, Integer.getInteger(port)));
                    }
                    else if(line.contains(pName_photoBasePath)) {
                        int sIndex = line.indexOf(pSeparator) + 1;
                        while(line.charAt(sIndex) == pSeparator || line.charAt(sIndex) == ' ') {
                            ++sIndex;
                        }
                        photoBasePath = line.substring(sIndex);
                    }
                    else if(line.contains(pName_targetPhotosPath)) {
                        int sIndex = line.indexOf(pSeparator) + 1;
                        while(line.charAt(sIndex) == pSeparator || line.charAt(sIndex) == ' ') {
                            ++sIndex;
                        }
                        targetPhotosPath = line.substring(sIndex);
                    }
                    // Parsing this shit here
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            return false;
        }
        return true;
    }

    public List<ClassifierAddress> getClassifiers()
    {
        return classifierAddress;
    }

    public String getPhotoBasePath()
    {
        return photoBasePath;
    }

    public String getTargetPhotosPath()
    {
        return targetPhotosPath;
    }
}

