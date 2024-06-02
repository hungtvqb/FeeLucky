/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mono.util;

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

/**
 *
 * @author haizpt
 */
public class CallWS {
    public static final int HTTP_TIMEOUT = 30 * 1000; //ms
    private static final Logger log = Logger.getLogger(CallWS.class);

    private static String callWS(String wsURL, String inputXML, String soapAction, int retry, int timeout) {
        String result = "";
        String responseString;

        int i = 0;
        for (i = 0; i < retry; i++) {
//            log.debug("Try " + i + " Call WS got XML input: " + inputXML);

            try {
                URL url = new URL(wsURL);
                URLConnection connection = url.openConnection();
                HttpURLConnection httpConn = (HttpURLConnection) connection;
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                bout.write(inputXML.getBytes());

                byte[] b = bout.toByteArray();
                // Set the appropriate HTTP parameters.
                httpConn.setReadTimeout(timeout); // ms
                httpConn.setConnectTimeout(timeout); // ms
                httpConn.setRequestProperty("Content-Length", String.valueOf(b.length));
                httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
                httpConn.setRequestProperty("SOAPAction", soapAction);
                //            httpConn.setRequestProperty("Host", "10.58.51.56:80");

                httpConn.setRequestMethod("POST");
                httpConn.setDoOutput(true);
                httpConn.setDoInput(true);
                OutputStream out = httpConn.getOutputStream();
                //Write the content of the request to the outputstream of the HTTP Connection.
                out.write(b);
                out.close();
                //Ready with sending the request.
                //Read the response.
                InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
                BufferedReader in = new BufferedReader(isr);

                //Write the SOAP message response to a String.
                while ((responseString = in.readLine()) != null) {
                    result += responseString;
                }
            } catch (Exception ex) {
                log.error(ex);
                result = "";
            }

            if (result != null && !result.isEmpty()) {
                break;
            }
        }
//        log.debug("Try " + i + " Call WS got Response: '" + result + "'");
        return result;
    }

    /**
     * Auto convert content to HEX or not
     *
     * @param content
     * @return
     */
    private static String convertContent(String content, boolean useHex) {
        if (useHex) {                      
                String charSet = CharsetDetector.detectCharsetStr(content);
                if (charSet != null && !charSet.equals("ASCII")) {
                    try {
                        return byteArrToHexStr(content.getBytes(charSet));
                    } catch (Exception ex) {
                        return byteArrToHexStr(content.getBytes());
                    }
                } else {
                    return byteArrToHexStr(content.getBytes());
                }
        } else {
            return content;
        }
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

    public static final String PARAMS_SMS_TEXT = "TEXT";
    public static final String PARAMS_SMS_FLASH = "FLASH";
    public static final String PARAMS_SMS_BINARY = "BINARY";
    public static final String PARAMS_SMS_HEX_TEXT = "HEX_TEXT";
    public static final String PARAMS_SMS_HEX_FLASH = "HEX_FLASH";
    
    public static int sendMT(String url, String username, String password, 
                       String msisdn, String content, String shortCode, String alias, String smsType, int retry) {
        int result = -1;
        if (content == null || content.isEmpty()) {
            log.warn("SendMT is called with Empty SMS");
            return result;
        }
        
        long start = System.currentTimeMillis();
        String soapAction = url;
        String newContent = convertContent(content,true);
        
        String xmlInput = 
                "<?xml version=\"1.0\" ?>" + 
                "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
                "<S:Body>" + 
                    "<smsRequest xmlns=\"http://smsws/xsd\">" + 
                        "<username>" + username + "</username>" + 
                        "<password>" + password + "</password>" + 
                        "<msisdn>" + msisdn + "</msisdn>" + 
                        "<content>" + newContent + "</content>" + 
                        "<shortcode>" + shortCode + "</shortcode>" + 
                        "<params>" + PARAMS_SMS_HEX_TEXT + "</params>" + 
                    "</smsRequest>" + 
                "</S:Body>" + 
                "</S:Envelope>";
        try {
            String xmlOut = callWS(url, xmlInput, soapAction, retry, 30000);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            //parse using builder to get DOM representation of the XML file
            InputStream inp = new ByteArrayInputStream(xmlOut.getBytes());
            Document doc = db.parse(inp);
            
            result = Integer.parseInt(doc.getElementsByTagName("return").item(0).getTextContent());
            log.info("Send SMS to '" + msisdn + "' -> " + result + 
                    " (" + (System.currentTimeMillis() - start) + " ms)");
        } catch (Exception e) {
            log.error("error when send sms",e);
            System.err.println(e);
            e.printStackTrace();
            result = -1;
        }
        return result;
    }

    /**
     * Replace values to variable in POST XML
     *
     * @param xml
     * @param replace
     * @return
     */
    public static String parseValue(String input, Properties replace) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String result = input;

        Enumeration<Object> key = replace.keys();
        while (key.hasMoreElements()) {
//            log.Debug("RESULT: '" + result + "'");
            String k = (String) key.nextElement();
            String val = replace.getProperty(k);
//            log.Debug("Replace '" + k + "' by '" + val + "'");
            result = result.replace(k, val);
        }
        return result;
    }
}
