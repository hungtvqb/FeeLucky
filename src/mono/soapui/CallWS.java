/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mono.soapui;

/**
 *
 * @author hungtv.hut
 */
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CallWS {

    public static final int HTTP_TIMEOUT = 30000;
    protected final static Logger log = Logger.getLogger(CallWS.class);

    public static String callWS(String wsURL, String inputXML) {
        return callWS(wsURL, inputXML, "POST", 1, 30000);
    }

    private static String callWS(String wsURL, String inputXML, String soapAction, int retry, int timeout) {
        String result = "";

        int i = 0;
        for (i = 0; i < retry; i++) {
            try {
                URL url = new URL(wsURL);
                URLConnection connection = url.openConnection();
                HttpURLConnection httpConn = (HttpURLConnection) connection;
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                bout.write(inputXML.getBytes());

                byte[] b = bout.toByteArray();

                httpConn.setReadTimeout(timeout);
                httpConn.setConnectTimeout(timeout);
                httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
                httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
                httpConn.setRequestProperty("SOAPAction", soapAction);

                httpConn.setRequestMethod("POST");
                httpConn.setDoOutput(true);
                httpConn.setDoInput(true);
                OutputStream out = httpConn.getOutputStream();

                out.write(b);
                out.close();

                InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
                BufferedReader in = new BufferedReader(isr);
                String responseString;
                while ((responseString = in.readLine()) != null) {
                    result = result + responseString;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                result = "";
            }

            if ((result != null) && (!result.isEmpty())) {
                break;
            }
        }
        return result;
    }

    private static String convertContent(String content, boolean useHex) {
        if (useHex) {
            String charSet = CharsetDetector.detectCharsetStr(content);
            if ((charSet != null) && (!charSet.equals("ASCII"))) {
                try {
                    return byteArrToHexStr(content.getBytes(charSet));
                } catch (Exception ex) {
                    return byteArrToHexStr(content.getBytes());
                }
            }
            return byteArrToHexStr(content.getBytes());
        }

        return content;
    }

    public static String byteArrToHexStr(byte[] var) {
        String tmp = "";
        String result = "";
        for (int i = 0; i < var.length; i++) {
            tmp = Integer.toHexString(var[i]).toUpperCase();
            if (tmp.length() < 2) {
                tmp = "0" + tmp;
            } else if (tmp.length() > 2) {
                tmp = tmp.substring(tmp.length() - 2);
            }
            result = result + tmp;
        }
        return result;
    }

    public static String sendMT(String url, String username, String password, String msisdn, String content, String shortCode, String alias, String smsType, int retry) {
        if ((content == null) || (content.isEmpty())) {
            return "2";
        }
        try {
            long start = System.currentTimeMillis();
            String soapAction = url;

            String newContent = convertContent(content, false);
            if (smsType.equals("FLASH")) {
                newContent = content;
            }
            String xmlInput = "<?xml version=\"1.0\" ?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body><smsRequest xmlns=\"http://smsws/xsd\"><username>" + username + "</username>" + "<password>" + password + "</password>" + "<msisdn>" + msisdn + "</msisdn>" + "<content>" + newContent + "</content>" + "<shortcode>" + shortCode + "</shortcode>" + "<alias>" + alias + "</alias>" + "<params>" + smsType + "</params>" + "</smsRequest>" + "</S:Body>" + "</S:Envelope>";

            String xmlOut = callWS(url, xmlInput, soapAction, retry, 30000);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            InputStream inp = new ByteArrayInputStream(xmlOut.getBytes());
            Document doc = db.parse(inp);

            String result = doc.getElementsByTagName("return").item(0).getTextContent();
            return result;
        } catch (Exception e) {
        }
        return "2";
    }

    public static String parseValue(String input, Properties replace) {
        if ((input == null) || (input.isEmpty())) {
            return "";
        }

        String result = input;

        Enumeration key = replace.keys();
        while (key.hasMoreElements()) {
            String k = (String) key.nextElement();
            String val = replace.getProperty(k);

            result = result.replace(k, val);
        }
        return result;
    }

    public static String sendUssd(String url, String msisdn, String amount,
            String serviceName, String subService, String providerName,
            String subCpName, String category, String item,
            String command, String transId, String channel) {
        if ((amount == null) || (amount.isEmpty())) {
            return "2";
        }
        try {
            String soapAction = url;
            String xmlInput = "<?xml version=\"1.0\" ?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                    + "<S:Body><ws:doCharge><msisdn>" + msisdn + "</msisdn>"
                    + "<amount>" + amount + "</amount>"
                    + "<serviceName>" + serviceName + "</serviceName>"
                    + "<subService>" + subService + "</subService>"
                    + "<SUB_SERVICE>" + subService + "</SUB_SERVICE>"
                    + "<providerName>" + providerName + "</providerName>"
                    + "<subCpName>" + subCpName + "</subCpName>"
                    + "<category>" + category + "</category>"
                    + "<item>" + item + "</item>"
                    + "<command>" + command + "</command>"
                    + "<CMD>" + command + "</CMD>"
                    + "<transId>" + transId + "</transId>"
                    + "<channel>" + channel + "</channel>"
                    + "</ws:doCharge></S:Body>" + "</S:Envelope>";

            String xmlOut = callWS(url, xmlInput, soapAction, 1, 30000);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            InputStream inp = new ByteArrayInputStream(xmlOut.getBytes());
            Document doc = db.parse(inp);

            String result = doc.getElementsByTagName("return").item(0).getTextContent();
            return result;
        } catch (Exception e) {
        }
        return "2";
    }
    
    
    
    public static String charge(String url, String msisdn, String amount,
            String serviceName, String subService, String providerName,
            String subCpName, String category, String item,
            String command, String transId, String channel) {
        if ((amount == null) || (amount.isEmpty())) {
            return "2";
        }
        try {
            String soapAction = url;
            String xmlInput = "<?xml version=\"1.0\" ?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                    + "<S:Body><ws:doCharge>"
                    + "<msisdn>" + msisdn + "</msisdn>"
                    + "<amount>" + amount + "</amount>"
                    + "<serviceName>" + serviceName + "</serviceName>"
                    + "<subService>" + subService + "</subService>"
                    + "<providerName>" + providerName + "</providerName>"
                    + "<subCpName>" + subCpName + "</subCpName>"
                    + "<category>" + category + "</category>"
                    + "<item>" + item + "</item>"
                    + "<command>" + command + "</command>"
                    + "<CMD>" + command + "</CMD>"
                    + "<transId>" + transId + "</transId>"
                    + "<channel>" + channel + "</channel>"
                    + "</ws:doCharge></S:Body>" + "</S:Envelope>";

            String xmlOut = callWS(url, xmlInput, soapAction, 1, 30000);
            
//            log.info("xmlInput = " + xmlInput);
//            log.info("url = " + url);
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            InputStream inp = new ByteArrayInputStream(xmlOut.getBytes());
            Document doc = db.parse(inp);

            String result = doc.getElementsByTagName("return").item(0).getTextContent();
            return result;
        } catch (Exception e) {
        }
        return "2";
    }
}
