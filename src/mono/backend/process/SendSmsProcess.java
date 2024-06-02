package mono.backend.process;

import mono.backend.bean.SmsData;
import static mono.backend.process.SendSmsProcess.log;
import mono.util.CallWS;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 */
public class SendSmsProcess {

    protected final static Logger log = Logger.getLogger(SendSmsProcess.class);
    private final static int THREAD_SEND_SMS = 5;

    private final LinkedHashMap<String, SmsData> queue = new LinkedHashMap<>();
    private final Object lock = new Object();

    private static SendSmsProcess _instance = null;

    private SendSmsProcess() {
    }

    public static SendSmsProcess getInstance() {
        if (_instance == null) {
            _instance = new SendSmsProcess();
            for (int i = 0; i < THREAD_SEND_SMS; i++) {
                SmsSender sender = new SmsSender(i, _instance);
                Thread thread = new Thread(sender);
                thread.setName("SendSmsProcess-" + i);
                thread.start();
            }
        }
        return _instance;
    }

    public void enqueue(SmsData sms) {
        synchronized (lock) {
            queue.put(sms.getId(), sms);
        }
    }

    public SmsData dequeue() {
        synchronized (lock) {
            if (queue.isEmpty()) {
                return null;
            }

            for (Map.Entry<String, SmsData> entry : queue.entrySet()) {
                String key = entry.getKey();
                SmsData smsData = entry.getValue();
                if (System.currentTimeMillis() > smsData.getSendTime()) {
                    return queue.remove(key);
                }
            }

            return null;
        }
    }

}

class SmsSender implements Runnable {

    private final String threadName;
    private final SendSmsProcess queue;

    public SmsSender(int i, SendSmsProcess queue) {
        this.threadName = SmsSender.class.getName() + "-" + i;
        this.queue = queue;
    }

    public String getThreadName() {
        return this.threadName;
    }

    @Override
    public void run() {
        while (true) {
            try {
                SmsData smsData = queue.dequeue();
                if (smsData != null) {
                    process(smsData);
                } else {
                    Thread.sleep(3000);// sleep 3s
                }
            } catch (Exception ex) {
                log.error(ex.toString(), ex);
            }
        }
    }

    private void process(SmsData sms) {
        int result = CallWS.sendMT(sms.getUrl(), sms.getUser(), sms.getPass(),
                sms.getMsisdn(), sms.getContent(), sms.getShortCode(),
                sms.getAlias(), sms.getSmsType(), 1);

        log.info(getThreadName() + ": send result " + result + ", " + sms);
    }

    public static void main(String[] args) {

        // sent SMS to sub
//        String url = GlobalConfig.getSmsUrl();
//        String user = GlobalConfig.getSmsUsername();
//        String pass = GlobalConfig.getSmsPassword();
//        String shortCode = GlobalConfig.getSmsShortcode();
//        String alias = GlobalConfig.getSmsAlias();
        Character a = 'V';
        int i = Character.getNumericValue(a);
        System.out.println("mono.backend.process.SmsSender.main()==========" + i);
//        SmsData sms = new SmsData("http://localhost:8250/smsws?wsdl", "okara", "okara123@", "8562097640951", "TEST", "1155", "1155", "TEXT");

//        CallWS.sendMT("http://localhost:8250/smsws?wsdl", "karaoke", "karaoke",
//                "8562097640951", CONST.CHARGEFEESUCCESS, "1155",
//                "1155", "TEXT", 1);
    }
}
