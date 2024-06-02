/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mono;

import java.util.ArrayList;
import java.util.List;
import mono.backend.database.DatabaseConnectionPool;
import mono.backend.database.DirectConnection;
import mono.backend.process.ProcessChargeFee;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

public class StartProcess extends Thread {

    protected final static Logger log = Logger.getLogger(StartProcess.class);
    private static List<ProcessChargeFee> list_ProcessChargeFee = new ArrayList();
    private static int que_Inserting = 0;

    public StartProcess() {
        log.info("Config.number_process: " + GlobalConfig.getNumberProcess());

        for (int i = 0; i < GlobalConfig.getNumberProcess(); i++) {
            ProcessChargeFee processChargeFee = new ProcessChargeFee(i);
            this.list_ProcessChargeFee.add(processChargeFee);
        }
    }

    @Override
    public void run() {
        try {
            for (ProcessChargeFee processCharge : this.list_ProcessChargeFee) {
                processCharge.start();
            }
            String chargeHour = GlobalConfig.getChargeFeeHour();
            log.info("[StartProcess]======>(Process charge " + chargeHour);
            log.info("Configure CHARGE_HOUR = " + chargeHour);
            String[] hourC = chargeHour.split(",");
            while (true) {
                for (String string : hourC) {
                    try {
                        int hour = Integer.parseInt(string.trim());
                        DateTime current = DateTime.now();

                        if (current.getHourOfDay() == hour) {
                            Thread.sleep(1000);
                            log.info("[StartProcess]======>(LOAD SUBSERVICE) time: " + hour + "h");
                            DirectConnection connect = null;
                            String sql = "SELECT MSISDN,STATUS,MONFEE_SUCCESS_COUNT,EXTRACT(DAY FROM (sysdate-LAST_MONFEE_CHARGE_TIME)) AS DAYLASTCHARGE FROM subscriber WHERE STATUS in (1,2) and NEXT_MONFEE_CHARGE_TIME < trunc(sysdate)";
                            try {
                                connect = DatabaseConnectionPool.getInstance().getConnection();
                                ArrayList<String> dataM = connect.queryArr(sql);
                                System.out.println("size = " + dataM.size());
                                log.info("size = " + dataM.size());
                                for (String data : dataM) {
                                    payLoadData(data);
                                }
                            } catch (Exception e) {
                                log.error("EXCEPTION" + e.getMessage());
                            }

                            log.info("[StartProcess]======>(LOAD COMPLETE) sleep: 1h");
                            Thread.sleep(3600000);
                        }
                    } catch (InterruptedException ex) {
                        log.error("error while loop:" + ex.getMessage());
                        Thread.sleep(30 * 1000);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[StartProcess]=========> run Exception" + e.getMessage());
        }
    }

    public void payLoadData(String data) {
        ProcessChargeFee chargeFee = this.list_ProcessChargeFee.get(this.que_Inserting);
        chargeFee.enqueue(data);
        this.que_Inserting += 1;
        if (this.que_Inserting >= GlobalConfig.getNumberProcess()) {
            this.que_Inserting = 0;
        }
    }
}
