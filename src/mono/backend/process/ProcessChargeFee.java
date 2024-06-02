package mono.backend.process;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import mono.GlobalConfig;
import redis.clients.jedis.Jedis;
import static mono.Start.queueUpdateExpire;
import mono.backend.bean.SmsData;
import mono.backend.database.DBFuction;
import mono.backend.database.RedisConnectPool;
import mono.soapui.CallWS;
import mono.util.CONST;
import org.apache.log4j.Logger;

public class ProcessChargeFee extends Thread {

    protected final static Logger log = Logger.getLogger(ProcessChargeFee.class);
    private int countInData;
    private int countOutData;
    private final int index;
    private final BlockingQueue<String> que_Process_Charge;

    private long lastReportTPS;
    private static final int STATISTICS_SEQUENCE = 20000;

    public ProcessChargeFee(int index) {
        this.index = index;
        this.que_Process_Charge = new ArrayBlockingQueue(10000);
    }

    @Override
    public void run() {
        Jedis jedis = null;
        try {
            jedis = RedisConnectPool.getInstance().getConnectionRedis();
            while (true) {
                try {
                    String infoSub = dequeue();
                    if (infoSub != null) {
                        String msisdn = infoSub.split("_")[0];
                        int status = Integer.parseInt(infoSub.split("_")[1]);
                        int numberCharge = Integer.parseInt(infoSub.split("_")[2]);
                        int dayLastCharge = Integer.parseInt(infoSub.split("_")[3]);
                        boolean checksendSMS = jedis.exists(CONST.R_BLL + msisdn);
                        if (msisdn != null) {
                            boolean check_charge_inday = jedis.exists(CONST.R_CHARGED_INDAY + msisdn);
                            if (!check_charge_inday) {
                                log.info("GET MSISDN FROM QUEUE TO CHARGE = " + msisdn);
                                processdata(msisdn, status, numberCharge, dayLastCharge, checksendSMS, jedis);
                                statisticsTPS();
                            } else {
                                //neu da charge roi ma charge nua thi bao loi ngay
                                log.warn("DA CHARGE O CHU KY TRUOC ...cho msisdn = " + msisdn);
                                queueUpdateExpire.add(msisdn + "_" + numberCharge);
                            }
                        } else {
                            Thread.sleep(50);
                            if (this.index > GlobalConfig.getNumberProcess()) {
                                this.yield();
                            }
                        }
                    }

                } catch (Exception e) {
                    log.error("Exception ============== " + e.getMessage());
                    if (jedis != null) {
                        RedisConnectPool.getInstance().returnResource(jedis);
                    }
                }
            }

        } catch (Exception ex) {
            log.error(ex);
            log.error("Exception =======xxxxxx======= " + ex.getMessage());
        }
    }

    public void enqueue(String data) {
        synchronized (this) {
            try {
                this.countInData += 1;
                this.que_Process_Charge.add(data);
            } catch (Exception x) {
                log.error("ERROR" + x);
            }
        }
    }

    private String dequeue() {
        synchronized (this) {
            return (String) this.que_Process_Charge.poll();
        }
    }

    private void statisticsTPS() {
        this.countOutData += 1;
        long duration = System.currentTimeMillis() - this.lastReportTPS;
        if (duration < STATISTICS_SEQUENCE) {
            //neu < 1 phut thi ko can in log
            return;
        }
        // in ra log neu xu ly nhieu hon 1 phut
        log.info(new StringBuilder().append("STATISTIC:").append(this.index).append("; SIZEQUEUE:").append(this.que_Process_Charge.size()).append(" ;OUT:").append(this.countOutData).append("; IN: ").append(this.countInData).append(" ;duration ").append(duration).append("(ms)").toString());
        this.countOutData = 0;
        this.countInData = 0;
        this.lastReportTPS = System.currentTimeMillis();
    }

    public void processdata(String msisdn, int status, int numberCharge, int dayLastCharge, boolean checksendSMS, Jedis jedis) {
        String response = "0";
        int responseInt = 0;
        try {
//            log.info("Charge for msisdn = " + msisdn);
            long timeStart = System.currentTimeMillis();

            String transactionID = msisdn + System.currentTimeMillis();
            response = CallWS.charge(GlobalConfig.getURL(), msisdn, GlobalConfig.getAmount(),
                    GlobalConfig.getServiceName(), GlobalConfig.getSubService(), GlobalConfig.getProviderName(),
                    GlobalConfig.getSubCpName(), GlobalConfig.getCategory(), GlobalConfig.getItem(),
                    GlobalConfig.getCommand(),
                    transactionID, GlobalConfig.getChannel());
            log.info("response charge of msisdn = " + msisdn + " is " + response + " ,time process is " + (System.currentTimeMillis() - timeStart) + " ms");

            response = response.trim();
            try {
                responseInt = Integer.parseInt(response.trim());
            } catch (Exception e) {
                responseInt = 2;
            }
            if (responseInt == 0) {
                //update exprie_time
                log.info("CHARGE THANH CONG CHO MSISDN = " + msisdn);
                numberCharge = numberCharge + 1;
                boolean updateExprire = DBFuction.updateExprire(msisdn, numberCharge);
                // sent SMS to sub
                log.info("updateExprire = " + updateExprire);
                if (updateExprire) {
                    String url = GlobalConfig.getSmsUrl();
                    String user = GlobalConfig.getSmsUsername();
                    String pass = GlobalConfig.getSmsPassword();
                    String shortCode = GlobalConfig.getSmsShortcode();
                    String alias = GlobalConfig.getSmsAlias();
                    if (checksendSMS) {
                        return;
                    }

                    jedis.set(CONST.R_CHARGED_INDAY + msisdn, "1");
                    jedis.expire(CONST.R_CHARGED_INDAY + msisdn, CONST.R_CHARGED_TIME_CACHE_D);

//                    log.info("so lan da charge thanh cong lien tiep la = " + numberCharge + " of msisdn = " + msisdn);
                    if (numberCharge >= 3) {
                        //gui sms cho khach hang thong bao charge thanh cong
                        log.info("CHARGE 3 LAN LIEN TIEP THANH CONG, GUI SMS CHO " + msisdn);
                        String MT = CONST.CHARGEFEESUCCESS;
                        SmsData sms = new SmsData(url, user, pass, msisdn, MT, shortCode, alias, "TEXT");
                        SendSmsProcess.getInstance().enqueue(sms);
                    }

                } else {
                    queueUpdateExpire.add(msisdn + "_" + numberCharge);
                    log.info("updateExprire error for " + msisdn + " update again");
                }

            } else {
                //xoa cache de lan sau charge tiep
                jedis.del(CONST.R_CHARGED_INDAY + msisdn);
                if (status == 1) {
                    //update status = 2
                    DBFuction.updateStatus(msisdn, 2);

                } else {
                    if (dayLastCharge >= 30) {
                        //cancel
                        DBFuction.updateCancel(msisdn, 0);
                        //charge gia 0 dong
                        CallWS.charge(GlobalConfig.getURL(), msisdn, "0",
                                GlobalConfig.getServiceName(), GlobalConfig.getSubService(), GlobalConfig.getProviderName(),
                                GlobalConfig.getSubCpName(), GlobalConfig.getCategory(), GlobalConfig.getItem(),
                                "CANCEL",
                                transactionID, GlobalConfig.getChannel());
                        log.info("QUA 30 ngay no cuoc, huy dich vu cho sub");
                    }
                }
            }

        } catch (Exception e) {
            log.error("Exception e = " + e.getMessage());
        }
    }
}
