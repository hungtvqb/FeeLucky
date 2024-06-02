package mono.backend.process;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import mono.GlobalConfig;
import mono.backend.bean.SmsData;
import mono.backend.database.DBFuction;
import org.apache.log4j.Logger;

public class processBaocaoDoanhThu extends Thread {

    protected final static Logger log = Logger.getLogger(processBaocaoDoanhThu.class);
    SimpleDateFormat formatDaily = new SimpleDateFormat("yyyy/MM/dd: hh/mm");

    @Override
    public void run() {
        try {
            while (true) {
                try {

                    int timeHour = new Date().getHours();
                    if ((timeHour > 6) && (timeHour <= 23)) {
                        String MT = "REPORT LUCKY: " + formatDaily.format(new Date()) + ";" + DBFuction.reportService();

                        String url = GlobalConfig.getSmsUrl();
                        String user = GlobalConfig.getSmsUsername();
                        String pass = GlobalConfig.getSmsPassword();
                        String shortCode = GlobalConfig.getSmsShortcode();
                        String alias = GlobalConfig.getSmsAlias();

                        SmsData sms1 = new SmsData(url, user, pass, "8562097640951", MT, shortCode, alias, "TEXT");
                        SmsData sms2 = new SmsData(url, user, pass, "8562096642520", MT, shortCode, alias, "TEXT");

                        SendSmsProcess.getInstance().enqueue(sms1);
                        SendSmsProcess.getInstance().enqueue(sms2);
                    }
                    Thread.sleep(60 * 60 * 1000);

                } catch (Exception e) {
                    log.error(e);
                    log.error("Mat ket noi redis");
                }
            }

        } catch (Exception e) {
        }
    }

}
