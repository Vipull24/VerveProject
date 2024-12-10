package org.verve;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static ConfigLoader instance;
    private Properties properties = new Properties();

    // Private constructor to restrict instantiation
    private ConfigLoader() {
        try (FileInputStream input = new FileInputStream("./verve.properties")) {
            // Load properties file from the specified file path
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Method to provide global access to the instance
    public static ConfigLoader getInstance() {
        if (instance == null) {
            synchronized (ConfigLoader.class) {
                if (instance == null) {
                    instance = new ConfigLoader();
                }
            }
        }
        return instance;
    }

    // Method to get the property value by key
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    // Overloaded method to get the property value with a default
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}