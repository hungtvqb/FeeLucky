package mono.backend.process;

import java.util.ArrayList;
import java.util.List;
import mono.Start;
import org.apache.log4j.Logger;
import org.bson.Document;

public class LogChargeFee extends Thread {

    private static final int ITERATION_COUNT = 200;
    protected final static Logger log = Logger.getLogger(LogChargeFee.class);

    public LogChargeFee() {
    }

    @Override
    public void run() {
        log.info("Log Transaction Charge Start.");
        try {
            while (true) {
                try {
                    if (!Start.queueLogChargeFee.isEmpty()) {
                        int iteration_count;
                        int queueSize = Start.queueLogChargeFee.getSize();
                        if (queueSize > ITERATION_COUNT) {
                            iteration_count = ITERATION_COUNT;
                        } else {
                            iteration_count = queueSize;
                        }
                        int count = 0;
                        List<Document> documents = new ArrayList<>();

                        while (count < iteration_count) {
                            if (Start.queueLogChargeFee.getSize() > 0) {
                                ChargeLog objLog = (ChargeLog) Start.queueLogChargeFee.poll();
                                Document document = new Document();
                                document.put("mobile", objLog.getMobile());
                                document.put("response", objLog.getResponse());
                                document.put("timeCharge", objLog.getTimeCharge());
                                document.put("price", objLog.getPrice());
                                document.put("mss", objLog.getMss());

                                if (objLog != null) {
                                    documents.add(document);
                                    count++;
                                } else {
                                    Thread.sleep(100);
                                }
                            } else {
                                Thread.sleep(100);
                            }
                        }
                        iteration_count = documents.size();
                        if (iteration_count > 0) {
                            log.info("Log Transaction Process: @queueINTransaction Log remain " + Start.queueLogChargeFee.getSize() + " records");
                        }
                    }

                } catch (Exception ex) {
                    log.error("error while loop:" + ex.getMessage());
                    log.error(ex);
                    Thread.sleep(30 * 1000);
                }

                Thread.sleep(10000);
            }
        } catch (Exception e) {
            log.error("Log ChargeFee Process: " + e);
        }

        log.info("Log ChargeFee Process Stop.");
    }

}
