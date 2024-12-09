package org.verve.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient {

    private static JedisPool jedisPool;

    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10000); // Set the maximum number of connections
        jedisPool = new JedisPool(poolConfig, "localhost", 6379);
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