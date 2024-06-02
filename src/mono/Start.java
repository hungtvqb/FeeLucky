/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mono;

//import mono.backend.process.SendSmsProcess;
import mono.soap.handle.MOHandler;
import mono.util.CountryCode;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import mono.backend.database.ConnectionPoolManager;
import mono.backend.database.DatabaseConnectionPool;
import mono.backend.database.RedisConnectPool;
import mono.backend.process.MsgQueue;
import mono.backend.process.SendSmsProcess;
import mono.backend.process.processBaocaoDoanhThu;
import mono.backend.process.processUpdateExpireAgain;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Start {

    protected final static Logger log = Logger.getLogger(Start.class);
    static final String copyright
            = "Developed by (@) 2018 Laos \n"
            + "This product includes software developed by hungtv whom copyright\n"
            + "and know-how are retained, all rights reserved.\n";

    static {
        System.out.println(copyright);
        log.info(copyright);
    }

    public final static Map<String, String> handleMapper = new HashMap<>();
    public static MsgQueue queueLogChargeFee = new MsgQueue();
    public static MsgQueue queueUpdateExpire = new MsgQueue();

    /**
     * @param args the command line arguments$
     */
    public static void main(String args[]) throws Exception {
        System.out.println("=====================================================: ");
        log.info("=====================================================:");
        //load configure
        configFile();
        // init Redis
        String dbRedisHost = GlobalConfig.getRedisHost();
        log.info("get Connection Redis " + dbRedisHost);
        RedisConnectPool.getInstance().load(dbRedisHost, 6379);
        startProcess();

        StartProcess startProcess = new StartProcess();
        startProcess.start();

    }

    public static Map<String, String> mapHandle() {
        synchronized (handleMapper) {
            if (handleMapper.isEmpty()) {
                handleMapper.put("/MoHandler", MOHandler.class.getName());
            }
            return handleMapper;
        }
    }

    private static void configFile() throws Exception {
        String home = System.getProperty("home");
        if (home != null && !home.isEmpty()) {
            if (!home.endsWith(File.separator)) {
                home = home + File.separator;
            }
            GlobalConfig.HOME_CONFIG = home;
        }

        PropertyConfigurator.configure(GlobalConfig.HOME_CONFIG + GlobalConfig.CONFIG_LOG4J);
        GlobalConfig.config(GlobalConfig.HOME_CONFIG + GlobalConfig.CONFIG_APP);
        CountryCode.config(GlobalConfig.HOME_CONFIG + GlobalConfig.CONFIG_APP);

        ConnectionPoolManager.loadConfig(GlobalConfig.HOME_CONFIG + GlobalConfig.CONFIG_DATABASE);
        Vector<String> v = ConnectionPoolManager.getConnnectionString();
        if (v == null) {
            throw new Exception("can not get string connection db");
        }
        DatabaseConnectionPool.config(v, GlobalConfig.getInt("db_pool_size", 2));
    }

    private static void startProcess() {

//        System.out.println("[Main] ====> start Process SendSmsProcess");
//        log.info("[Main] ====> start Process SendSmsProcess");
//        SendSmsProcess.getInstance();

        System.out.println("[Main] ====> start Process processUpdateExpireAgain");
        log.info("[Main] ====> start Process processUpdateExpireAgain");
        processUpdateExpireAgain processUpdateE = new processUpdateExpireAgain();
        processUpdateE.start();

        processBaocaoDoanhThu doanhthu = new processBaocaoDoanhThu();
        doanhthu.start();
    }
}
