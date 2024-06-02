/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mono.soap.handle;

import mono.GlobalConfig;
import mono.backend.bean.SmsData;
import mono.backend.database.DBFuction;
import mono.backend.process.SendSmsProcess;
//import mono.backend.process.SendSmsProcess;
import mono.soap.SoapRequest;
import mono.soapui.CallWS;
import mono.util.CONST;
import mono.util.CountryCode;

/**
 *
 * <
 * soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
 * xmlns:soap="http://soap.mps.ra.rabbit.vtech.com/"><soapenv:Header/><soapenv:Body>
 * <soap:resultRequest>
 * <msisdn>#MSISDN</msisdn>
 * <transid>#TRANSACTIONID</transid>
 * <content>#CONTENT</content>
 * </soap:resultRequest>
 * </soapenv:Body></soapenv:Envelope>
 */
public class MOHandler extends AbstractHandler {

    public MOHandler(SoapRequest request) {
        super(request);
    }

    @Override
    public String process(SoapRequest request) {
        String value = "0";
        try {
            log.info("receive: " + request.getRawXml());
            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                log.error(request.getClientIp() + " , send Method: " + request.getMethod());
                return "1|Method:  " + request.getMethod();
            }

            String msisdn = request.getDatas().get("source");
            if (msisdn == null) {
                return "1|source:  null";
            }

            msisdn = CountryCode.formatMobile(msisdn);
            String content = request.getDatas().get("content").trim();
            log.info("request MO: " + msisdn + "," + content);
            if (content == null) {
                return "1|content:  null";
            }
//            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String transactionID = msisdn + System.currentTimeMillis();
            String MT = "";
            content = content.replace(" ", "");
//            int checkSub = DBFuction.checkSub(msisdn);
            if ("ON".equals(content)) {
            } else if ("OFF".equals(content)) {


            } else {
                //KHONG HOP LE
            }
            // sent SMS to sub
            String url = GlobalConfig.getSmsUrl();
            String user = GlobalConfig.getSmsUsername();
            String pass = GlobalConfig.getSmsPassword();
            String shortCode = GlobalConfig.getSmsShortcode();
            String alias = GlobalConfig.getSmsAlias();
            
//            log.info("url = " + url);
//            log.info("user = " + user);
//            log.info("pass = " + pass);
//            log.info("shortCode = " + shortCode);
            
            
            
            if (content != null && !content.isEmpty()) {
                SmsData sms = new SmsData(url, user, pass, msisdn, MT, shortCode, alias, "TEXT");
                SendSmsProcess.getInstance().enqueue(sms);
            }

        } catch (Exception ex) {
            value = "1|" + ex.getMessage();
            log.error("error when receive renew request", ex);
        }
        return value;
    }

    public static void main(String[] args) {
//        Random random = new Random();
//        int randomS = random.nextInt(16);
//        System.out.println("randomS = " + randomS);
    }
}
