package com.tablu.mall.utils;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * PUBLIC_INTERFACE
 * KVHashStoreConfig
 *
 * Provides a KVHashStore bean for application components. If a Redis StringRedisTemplate is available
 * in the Spring context, a Redis-backed implementation will be returned; otherwise a memory-backed
 * implementation is used, ensuring the application can start without Redis configured.
 */
@Configuration
public class KVHashStoreConfig {

    /**
     * PUBLIC_INTERFACE
     * Returns the appropriate KVHashStore depending on whether Redis is present.
     *
     * @param redisTemplateProvider optional provider for StringRedisTemplate
     * @return KVHashStore backed by Redis if available, else memory
     */
    @Bean
    public KVHashStore kvHashStore(ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        StringRedisTemplate template = redisTemplateProvider.getIfAvailable();
        if (template != null) {
            return new RedisKVHashStore(template);
        }
        return new MemoryKVHashStore();
    }
}
