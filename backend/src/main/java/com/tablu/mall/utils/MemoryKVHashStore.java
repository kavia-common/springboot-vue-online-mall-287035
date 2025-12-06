package com.tablu.mall.utils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PUBLIC_INTERFACE
 * MemoryKVHashStore
 *
 * Thread-safe in-memory implementation of KVHashStore for development/preview usage.
 * Not intended for production use as it is not persistent and is per-JVM-instance.
 */
public class MemoryKVHashStore implements KVHashStore {

    // store structure: key -> (hashKey -> value)
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> store = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> entries(String key) {
        ConcurrentHashMap<String, String> map = store.get(key);
        if (map == null) {
            return Collections.emptyMap();
        }
        // Return a shallow copy to protect internal state
        return new ConcurrentHashMap<>(map);
    }

    @Override
    public String get(String key, String hashKey) {
        ConcurrentHashMap<String, String> map = store.get(key);
        return map == null ? null : map.get(hashKey);
    }

    @Override
    public void put(String key, String hashKey, String value) {
        store.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).put(hashKey, value);
    }

    @Override
    public void delete(String key, String hashKey) {
        ConcurrentHashMap<String, String> map = store.get(key);
        if (map != null) {
            map.remove(hashKey);
        }
    }
}
