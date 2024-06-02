/**
 *
 * handsome boy
 */

package mono.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.apache.axis.AxisFault;
import org.apache.axis.Message;


public class SoapClient {    
    private String url = null;

    public SoapClient(String url) {
        this.url = url;
    }
    
    public Map<String,String> sendRequest(String xml, final int timeout){
        Map<String,String> pro = null;
        try {
            SOAPConnection connection = SOAPConnectionFactory.newInstance().createConnection();
            URL endpoint = null;
            try {
                endpoint = new URL(null,
                        url,
                        new URLStreamHandler() {
                            @Override
                            protected URLConnection openConnection(URL url) throws IOException {
                                URL target = new URL(url.toString());
                                URLConnection connection = target.openConnection();
                                connection.setConnectTimeout(timeout); 
                                connection.setReadTimeout(timeout); 
                                return(connection);
                            }
                        });
            } catch (MalformedURLException ex) {
            }
            if (endpoint != null){
                SOAPMessage result = connection.call(getSoapBody(xml), endpoint);
                pro = new HashMap();
                convertSOAPBodyToProperties(result.getSOAPBody(),pro);
                connection.close();
            }
        } catch (SOAPException ex) {
        } catch (UnsupportedOperationException | AxisFault ex) {
        } 
        
        return pro;
    }
    
    private void convertSOAPBodyToProperties(SOAPElement message, Map pro) throws AxisFault, SOAPException {

        Iterator i = message.getChildElements();
        while (i.hasNext()) {
            SOAPElement element = (SOAPElement)i.next();

            String name = element.getNodeName();
            String value = element.getValue();
            if (value != null) {
                pro.put(name, value);
            } else {
                convertSOAPBodyToProperties(element, pro);
            }
       
        }
        
    }
    
    
    private SOAPMessage getSoapBody(String content) {
        SOAPMessage message = null;
        boolean soap1_2 = content.contains(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);
        
        try {
            if (soap1_2) {
                byte[] buffer = new byte[192];
                InputStream stream = new ByteArrayInputStream(buffer);
                message = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL)
                    .createMessage(null, stream);
            } else {
                 message = new Message(content);
            }
        } catch (SOAPException | IOException ex) {
            Logger.getLogger(SoapClient.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        MimeHeaders headers = message.getMimeHeaders();
        headers.addHeader("Accept-Charset", "utf-8");
        headers.addHeader("Content-Type", "text/xml");
        headers.addHeader("Accept", "application/soap+xml");
        headers.addHeader("Content-Length", String.valueOf(content.length()));
        return message;
    }
    
    /**
     * Method used to print the SOAP Response
     */
    private void printSOAPResponse(SOAPMessage soapResponse) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        Source sourceContent = soapResponse.getSOAPPart().getContent();
     
        System.out.println("\nResponse SOAP Message = ");
        StreamResult result = new StreamResult(System.out);
        transformer.transform(sourceContent, result);
        System.out.println("\nResponse SOAP Message end ");
    }
    
   
    
}
