/**
 *
 * handsome boy
 */

package mono.soap;

/**
 *
 * @author hungtv 
 * @since Jun 11, 2015
 */
public interface HttpHandler {
    public final static String METHOD_POST = "POST";
    public final static String METHOD_GET = "GET";
   
    public void process(SoapRequest request);
    
}
