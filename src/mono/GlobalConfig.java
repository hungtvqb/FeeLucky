/**
 *
 * handsome boy
 */
package mono;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GlobalConfig
 */
public class GlobalConfig {

    public static String HOME_CONFIG = "/u01/lucky/LUCKY/FeeLucky/etc/";
//    public static String HOME_CONFIG = "./etc/";

    public final static String CONFIG_APP = "config.cfg";
    public final static String CONFIG_LOG4J = "log4j.cfg";
    public final static String CONFIG_DATABASE = "database.xml";

    public static String REDIS_HOST;
    public static String CHARGE_HOUR;
    public static int REDIS_PORT = 6379;

    private static final Object obj = new Object();

    public static String configFilePath;

    public static ResourceBundle resourceBundle;
    public static String sms_url;
    public static String sms_username;
    public static String sms_password;
    public static String sms_shortcode;
    public static String sms_alias;

    //CHARGE
    public static String URL;
    public static String serviceName;
    public static String providerName;
    public static String subCpName;
    public static String category;
    public static String item;
    public static String command;
    public static String subService;
    public static String channel;
    public static String amount;
    public static int number_process;

    private static void config(InputStream inputStream) {
        try {
            resourceBundle = new PropertyResourceBundle(inputStream);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GlobalConfig.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GlobalConfig.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(GlobalConfig.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void config(String configFile) {
        configFilePath = configFile;
        FileInputStream fis;
        try {
            fis = new FileInputStream(configFile);
            config(fis);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GlobalConfig.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static String get(String key) {
        synchronized (obj) {
            try {
                return resourceBundle.getString(key);
            } catch (Exception ex) {
                Logger.getLogger(GlobalConfig.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }

    public static String getSmsUrl() {
        if (sms_url == null) {
            sms_url = get("sms.url");
        }
        return sms_url;
    }

    public static String getURL() {
        if (URL == null) {
            URL = get("charge.url");
        }
        return URL;
    }

    public static String getServiceName() {
        if (serviceName == null) {
            serviceName = get("charge.serviceName");
        }
        return serviceName;
    }

    public static String getProviderName() {
        if (providerName == null) {
            providerName = get("charge.providerName");
        }
        return providerName;
    }

    public static String getSubCpName() {
        if (subCpName == null) {
            subCpName = get("charge.subCpName");
        }
        return subCpName;
    }

    public static String getCategory() {
        if (category == null) {
            category = get("charge.category");
        }
        return category;
    }

    public static String getItem() {
        if (item == null) {
            item = get("charge.item");
        }
        return item;
    }

    public static String getCommand() {
        if (command == null) {
            command = get("charge.command");
        }
        return command;
    }

    public static String getSubService() {
        if (subService == null) {
            subService = get("charge.subService");
        }
        return subService;
    }

    public static String getChannel() {
        if (channel == null) {
            channel = get("charge.channel");
        }
        return channel;
    }

    public static String getAmount() {
        if (amount == null) {
            amount = get("charge.amount");
        }
        return amount;
    }

    public static int getNumberProcess() {
        String tm2 = get("number_process");
        if (tm2 != null) {
            number_process = Integer.parseInt(tm2);
        } else {
            number_process = 600;
        }

        return number_process;
    }

    public static String getSmsUsername() {
        if (sms_username == null) {
            sms_username = get("sms.username");
        }
        return sms_username;
    }

    public static String getSmsPassword() {
        if (sms_password == null) {
            sms_password = get("sms.password");
        }
        return sms_password;
    }

    public static String getSmsShortcode() {
        if (sms_shortcode == null) {
            sms_shortcode = get("sms.shortcode");
        }
        return sms_shortcode;
    }

    public static String getSmsAlias() {
        if (sms_alias == null) {
            sms_alias = get("sms.alias");
        }
        return sms_alias;
    }

    public static String getRedisHost() {
        if (REDIS_HOST == null) {
            REDIS_HOST = get("REDIS_HOST");
        }
        return REDIS_HOST;
    }

    public static String getChargeFeeHour() {
        if (CHARGE_HOUR == null) {
            CHARGE_HOUR = get("CHARGE_HOUR");
        }
        return CHARGE_HOUR;
    }

    public static int getInt(String key, int defaultValue) {
        int value = defaultValue;
        String tm = get(key);
        if (tm != null) {
            value = Integer.parseInt(tm);
        }

        return value;
    }

}
