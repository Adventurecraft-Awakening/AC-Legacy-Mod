package dev.adventurecraft.awakening.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;

public final class SystemPropertyUtil {

    private static final Logger logger = LoggerFactory.getLogger(SystemPropertyUtil.class);

    public static String get(String key, String def) {
        String value = null;
        try {
            if (System.getSecurityManager() == null) {
                value = System.getProperty(key);
            } else {
                value = AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(key));
            }
        } catch (SecurityException e) {
            logger.warn("Unable to retrieve a system property '{}'; default values will be used.", key, e);
        }

        if (value == null) {
            return def;
        }
        return value;
    }
}
