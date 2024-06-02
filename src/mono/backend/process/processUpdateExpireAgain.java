package mono.backend.process;

import mono.Start;
import mono.backend.database.DBFuction;
import org.apache.log4j.Logger;

public class processUpdateExpireAgain
        extends Thread {

    protected static final Logger log = Logger.getLogger(processUpdateExpireAgain.class);

    @Override
    public void run() {
        log.info("Log Transaction Charge Start.");
        try {
            for (;;) {
                try {
                    if (!Start.queueUpdateExpire.isEmpty()) {
                        String infoData = (String) Start.queueUpdateExpire.poll();
                        String msisdn = infoData.split("_")[0];
                        int numberCharge = Integer.parseInt(infoData.split("_")[1]);
                        boolean update = DBFuction.updateExprire(msisdn, numberCharge);
                        if (update) {
                            log.info("Update again thanh cong for " + msisdn);
                        } else {
                            log.info("Update again error for " + msisdn);
                        }
                    }
                    Thread.sleep(300);
                } catch (Exception ex) {
                    log.error("error while loop:" + ex.getMessage());
                    log.error(ex);
                    Thread.sleep(30000);
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            log.error("Log ChargeFee Process: " + e);

            log.info("Log ChargeFee Process Stop.");
        }
    }
}
