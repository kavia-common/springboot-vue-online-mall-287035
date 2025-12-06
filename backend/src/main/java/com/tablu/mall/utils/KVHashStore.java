package com.tablu.mall.utils;

import java.util.Map;

/**
 * PUBLIC_INTERFACE
 * KVHashStore
 *
 * Abstraction for a simple hash map-like key-value store. Designed to wrap Redis hash operations
 * for production and provide an in-memory fallback for development/preview environments where
 * Redis is not available.
 */
public interface KVHashStore {

    /**
     * PUBLIC_INTERFACE
     * Fetch all field-value pairs for a given top-level key (hash).
     *
     * @param key the top-level key for the hash
     * @return a map of field -> value; never null (empty if key not found)
     */
    Map<String, String> entries(String key);

    /**
     * PUBLIC_INTERFACE
     * Get a single field from a given hash key.
     *
     * @param key     the top-level key
     * @param hashKey the field key within the hash
     * @return the value or null if not found
     */
    String get(String key, String hashKey);

    /**
     * PUBLIC_INTERFACE
     * Put a field-value into a given hash key (create or update).
     *
     * @param key     the top-level key
     * @param hashKey the field key
     * @param value   JSON or stringified value
     */
    void put(String key, String hashKey, String value);

    /**
     * PUBLIC_INTERFACE
     * Delete a field from a given hash key.
     *
     * @param key     the top-level key
     * @param hashKey the field key to remove
     */
    void delete(String key, String hashKey);
}
