package com.tablu.mall.utils;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * PUBLIC_INTERFACE
 * RedisKVHashStore
 *
 * Redis implementation of KVHashStore. Uses StringRedisTemplate with Redis Hash operations.
 */
public class RedisKVHashStore implements KVHashStore {

    private final StringRedisTemplate redisTemplate;

    /**
     * PUBLIC_INTERFACE
     * Constructs a new RedisKVHashStore.
     *
     * @param redisTemplate injected StringRedisTemplate
     */
    public RedisKVHashStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Map<String, String> entries(String key) {
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        Map<String, String> data = ops.entries(key);
        // ops.entries may return null if key doesn't exist; normalize to empty map
        return data == null ? new HashMap<>() : data;
    }

    @Override
    public String get(String key, String hashKey) {
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        return ops.get(key, hashKey);
    }

    @Override
    public void put(String key, String hashKey, String value) {
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        ops.put(key, hashKey, value);
    }

    @Override
    public void delete(String key, String hashKey) {
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        ops.delete(key, hashKey);
    }
}
