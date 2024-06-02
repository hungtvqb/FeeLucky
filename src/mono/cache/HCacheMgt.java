/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mono.cache;

import java.util.List;
import java.util.Vector;

/**
 *
 * @author hungtv
 */
public class HCacheMgt {
    
    private static final  List<HCache> caches = new Vector();
    
    public synchronized static void register(HCache cache){
        caches.add(cache);
    }
    
    public synchronized static void clear(){
         for (HCache hCache : caches) {
                hCache.clear();
            }
    }

    
}
