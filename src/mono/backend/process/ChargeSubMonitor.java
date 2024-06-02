package mono.backend.process;

import mono.GlobalConfig;
import mono.backend.database.DBFuction;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

public class ChargeSubMonitor extends Thread {
    
    protected final static Logger log = Logger.getLogger(ChargeSubMonitor.class);
    
    @Override
    public void run() {
        log.info("[ChargeSubMonitor]======>(Process charge 4h, 8h, 13h, 19h, 22h)");
        String chargeHour = GlobalConfig.getChargeFeeHour();
        log.info("Configure CHARGE_HOUR = " + chargeHour);
        String[] hourC = chargeHour.split(",");
        
        try {
            while (true) {
                for (String string : hourC) {
                    try {
                        int hour = Integer.parseInt(string.trim());
                        DateTime current = DateTime.now();
                        if (current.getHourOfDay() == hour) {
                            Thread.sleep(1000);
                            log.info("[ChargeSubMonitor]======>(LOAD SUBSERVICE) time: " + hour + "h");
                            DBFuction.selectFeeOracle();
                            log.info("[ChargeSubMonitor]======>(LOAD COMPLETE) sleep: 1h");
                            Thread.sleep(3600000);
                        } else {
                            log.info("chargeHour = " + chargeHour);
                            Thread.sleep(2000);
                        }
                    } catch (InterruptedException ex) {
                        log.error("error while loop:" + ex.getMessage());
                        Thread.sleep(30 * 1000);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[ChargeSubMonitor]======> " + e);
        }
        log.info("[ChargeSubMonitor]======>(Process STOP) STOP");
        
    }
    
    public static long getTimeSleepToNextHour() {
        DateTime current = DateTime.now();
        int x = (60 - current.getMinuteOfHour()) * 60 + (60 - current.getSecondOfMinute());
        return x * 1000;
    }
}
