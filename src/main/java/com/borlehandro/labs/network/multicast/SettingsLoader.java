package com.borlehandro.labs.network.multicast;

import java.io.*;
import java.util.Properties;

public class SettingsLoader {

    private final Properties properties = new Properties();

    public enum Property {
        SOCKET_TIMEOUT("socketTimeout"),
        CHECK_USERS_INTERVAL("checkUsersInterval"),
        SAY_HELLO_INTERVAL("sayHelloInterval"),
        MAX_FAILED_CHECKS("maxFailedChecks");

        private final String nameInFile;

        Property(String nameInFile) {
            this.nameInFile = nameInFile;
        }

        public String getNameInFile() {
            return nameInFile;
        }
    }

    public SettingsLoader(InputStream input) {
        try {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long loadProperty(Property property) {
        return Long.parseLong(properties.get(property.getNameInFile()).toString());
    }

    public String loadMulticastIp() {
        return String.valueOf(properties.get("multicastIp"));
    }

    public int loadPort() {
        return Integer.parseInt(String.valueOf(properties.get("port")));
    }

}