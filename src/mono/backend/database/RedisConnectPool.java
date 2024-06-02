package mono.backend.database;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author hungtv.hut
 *
 */
public class RedisConnectPool {

    final JedisPoolConfig poolConfig = buildPoolConfig();
    JedisPool jedisPool = null;
    private static RedisConnectPool mPoolRedis;

    private JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfigb = new JedisPoolConfig();
        poolConfigb.setMaxTotal(100);
        poolConfigb.setMaxIdle(50);
        poolConfigb.setMinIdle(50);
        poolConfigb.setTestOnBorrow(false);
        poolConfigb.setTestOnReturn(true);
        poolConfigb.setTestWhileIdle(true);
        return poolConfigb;
    }

    public void load(String host, int port) {
        try {
            if (host != null) {
                this.jedisPool = new JedisPool(poolConfig, host, port);
            }
        } catch (Exception e) {
        }
    }

    public static RedisConnectPool getInstance() {
        if (mPoolRedis == null) {
            mPoolRedis = new RedisConnectPool();
        }
        return mPoolRedis;
    }

    public Jedis getConnectionRedis() {
        try {
            return jedisPool.getResource();
        } catch (Exception e) {
        }
        return null;
    }

    public void returnResource(Jedis jedis) {
        try {
            jedisPool.returnResource(jedis);
        } catch (Exception e) {
            if (jedis != null) {
                jedisPool.returnBrokenResource(jedis);
                jedis = null;
            }
            throw e;
        }
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }
}
