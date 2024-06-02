/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mono.cache;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hungtv
 */
public class HCache<K, V> {
    public static final int NO_MAX_CAPACITY = 0;
    public static final int NO_TIMEOUT = 0;
    public static final int DEFAULT_MAX_CAPACITY = 100;
    public static final int DEFAULT_TIMEOUT = 120;
    public static final int ONE_MINUTE = 60000;
    
     /**
     * Name
     */
    private String m_name = null;
    private volatile long m_expireTime = 0;
    private volatile int m_maxCapacity = 100;
    private long lastExpire = -1;

    private final Map<K,HCacheEntry<V>> datas;
    private final Object lock = new Object();
    
    public HCache(String name, int initialCapacity, int maxCapacity, int expireMinutes) {
        datas = new LinkedHashMap<>(initialCapacity);
        this.m_name = name;
        m_name = name;
        
        if (maxCapacity > NO_MAX_CAPACITY && maxCapacity < initialCapacity) {
            maxCapacity = initialCapacity;
        }
        m_maxCapacity = maxCapacity;
        m_expireTime = expireMinutes * ONE_MINUTE;
        HCacheMgt.register(this);
    }	//	CCache
    
    public String getName(){
        return m_name;
    }
    
    public void clear() {
        synchronized(lock) {
            datas.clear();
        }
    }
    
    public void put(K k, V v) {
        synchronized (lock){
            HCacheEntry<V> entry = new HCacheEntry<>(v);
            datas.put(k, entry);
        }
    }
    
    public V get(K k){
        synchronized (lock){
            expire();
            HCacheEntry<V> entry = datas.get(k);
            if (entry == null) {
                return null;
            } 
            return entry.getData();
        }
    }
    
    public boolean contains(K k) {
        synchronized (lock){
            return datas.containsKey(k);
        }
    }
    
    private void expire() {
        synchronized (lock){
            if (m_expireTime <= 0 ) {
                return;
            }

            if ((System.currentTimeMillis() - lastExpire) < ONE_MINUTE ){
                return;
            }

            List<K> expiredKeys = new LinkedList<>();

            for (Map.Entry<K, HCacheEntry<V>> entry : datas.entrySet()) {
                K k = entry.getKey();
                HCacheEntry<V> hCacheEntry = entry.getValue();
                long timeExp = hCacheEntry.getCacheUpdated() + m_expireTime;
                if (timeExp < System.currentTimeMillis()) {
                    expiredKeys.add(k);
                }
            }

            if (!expiredKeys.isEmpty()) {
                for (K k : expiredKeys) {
                    datas.remove(k);
                }
            }

            lastExpire = System.currentTimeMillis();
        }
    }	
}
