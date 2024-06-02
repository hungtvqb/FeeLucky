/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mono.cache;

/**
 *
 * @author hungtv
 */
public class HCacheEntry<V> {
  
    /**
     * The time this entry was created.
     */
    private long cacheCreated = -1;

    /**
     * The time this emtry was last updated.
     */
    private long cacheUpdated = -1;
    
    private final V data;
    
    public HCacheEntry(V data){
        this.data = data;
        cacheCreated = System.currentTimeMillis();
        cacheUpdated = System.currentTimeMillis();
    }
   
    public long getCacheCreated() {
    	return cacheCreated;
    }
    
    public V getData(){
        cacheUpdated = System.currentTimeMillis();
        return data;
    }
    
    public long getCacheUpdated(){
        return cacheUpdated;
    }
}
