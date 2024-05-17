package net.lexzeus.fx.simul;

import net.lexzeus.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class to read configuration from TradingStrategyConfiguration.properties
 * To override, set environment variable FX_SIMUL_CONFIG=[location of file]
 *
 * @author Alexander Koentjara
 */
public class Configuration {

    public static final String VERSION_KEY = "version";
    public static final String MARKET_DATA_DIR_KEY = "marketdata.dir";
    public static final String SNAPSHOT_DIR_KEY = "snapshot.dir";
    public static final String REPORT_DIR_KEY = "report.dir";
    public static final String FX_SIMUL_CONFIG_KEY = "FX_SIMUL_CONFIG";
    public static final String FX_SIMUL_CONFIG = "TradingStrategyConfiguration.properties";
    public static final Log log = Log.get(Configuration.class);
    private static final Properties prop;

    static {
        prop = new Properties();
        String propName = System.getenv(FX_SIMUL_CONFIG_KEY);
        if (propName == null) {
            propName = FX_SIMUL_CONFIG;
        }

        try (InputStream conf = Configuration.class.getClassLoader().getResourceAsStream(propName);) {
            prop.load(conf);
        } catch (IOException ex1) {
            File f = new File(propName);
            if (f.exists()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    prop.load(fis);
                } catch (IOException ex2) {
                }
            }
            log.error("Cannot find configuration file: " + propName);
        }
    }

    public static String getProperty(String key) {
        String value = prop.getProperty(key);
        return (value == null) ? System.getProperty(key) : value;
    }

    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return (value == null) ? defaultValue : value;
    }

    public static int getIntProperty(String key) {
        try {
            return Integer.parseInt(getProperty(key));
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
