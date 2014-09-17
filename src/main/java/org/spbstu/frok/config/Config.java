package org.spbstu.frok.config;

/**
 * Created by zda on 17.09.14.
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;

public class Config {
    Properties prop = new Properties();

    public static final String CONFIG_FILENAME = "/etc/frok/frok-ds.conf";

// Names of parameters
    public static final String PHOTO_BASE_PATH_PARAM = "PHOTO_BASE_PATH";
    public static final String TARGET_PHOTO_PATH_PARAM = "TARGET_PHOTOS_PATH";
    public static final String CLASSIFIER_ADDRESS_PARAM = "FROK_SERVER";

    private static Config INSTANCE;

    private Config() {}

    public static Config getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Config();
            INSTANCE.parseConfigFile(CONFIG_FILENAME);
        }
        return INSTANCE;
    }

    private void parseConfigFile(String fileName) {
        InputStream input = null;
        try {
            input = new FileInputStream(fileName);
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return;
    }
    public String getParamValue(String param) {
        return prop.getProperty(param);
    }
}

