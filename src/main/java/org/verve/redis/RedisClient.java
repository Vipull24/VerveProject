package org.verve.redis;

import org.verve.ConfigLoader;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient {

    private static JedisPool jedisPool;

    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10000); // Set the maximum number of connections
        String host = "localhost";
        if(ConfigLoader.getInstance().getProperty("redis.host") != null) {
            host = ConfigLoader.getInstance().getProperty("redis.host");
        }
        int port = 6379;
        if(ConfigLoader.getInstance().getProperty("redis.port") != null) {
            port = Integer.parseInt(ConfigLoader.getInstance().getProperty("redis.port"));
        }
        jedisPool = new JedisPool(poolConfig, host, port);
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }

    public static void closePool() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}