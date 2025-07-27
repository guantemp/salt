/*
 * Copyright (c) 2025. www.hoprxi.com All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package salt.hoprxi.cache.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.typesafe.config.Config;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import salt.hoprxi.cache.Cache;
import salt.hoprxi.cache.event.CacheStats;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.2 builder 2025-07-27
 */
public class CaffeineCache<K, V> implements Cache<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaffeineCache.class);
    private final com.github.benmanes.caffeine.cache.Cache<K, V> cache;
    private CacheStats stats = CacheStats.EMPTY_STATS;

    public static class Builder<K, V> implements salt.hoprxi.utils.Builder<CaffeineCache<K, V>> {
        private int capacity = 128;
        private long expireAfterAccess = 1_800_000L;//30 MINUTES
        private long expireAfterWrite = 600_000L;//10 MINUTES
        private long maximumSize = (long) (Runtime.getRuntime().maxMemory() * 0.15 / 1024);//15% memory
        private long refreshAfterWrite = 30_000L;//5 MINUTES
        private boolean recordStats;

        public Builder<K, V> capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder<K, V> refreshAfterWrite(long refreshAfterWrite) {
            this.refreshAfterWrite = refreshAfterWrite;
            return this;
        }

        public Builder<K, V> maximumSize(long maximumSize) {
            this.maximumSize = maximumSize;
            return this;
        }

        public Builder<K, V> expireAfterAccess(long expireAfterAccess) {
            this.expireAfterAccess = expireAfterAccess;
            return this;
        }

        public Builder<K, V> recordStats() {
            this.recordStats = true;
            return this;
        }

        public Builder<K, V> expireAfterWrite(long expireAfterWrite) {
            this.expireAfterWrite = expireAfterWrite;
            return this;
        }

        @Override
        public CaffeineCache<K, V> build() {
            return new CaffeineCache<>(expireAfterWrite, expireAfterAccess, refreshAfterWrite, maximumSize, capacity, recordStats);
        }
    }

    private CaffeineCache(long expireAfterWrite, long expireAfterAccess, long refreshAfterWrite, long maximumSize, int capacity, boolean recordStats) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        builder.expireAfterWrite(expireAfterWrite, TimeUnit.SECONDS)
                .expireAfterAccess(expireAfterAccess, TimeUnit.SECONDS)
                .refreshAfterWrite(refreshAfterWrite, TimeUnit.SECONDS)
                .initialCapacity(capacity)
                .maximumSize(maximumSize);
        if (recordStats)
            builder.recordStats();
        cache = builder.build();
    }
/*
    public CaffeineCache(Properties properties) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        cache = builder.build();
    }
 */

    /**
     * @param config
     */
    public CaffeineCache(Config config) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        if (config.hasPath("expireAfterWrite")) {
            builder.expireAfterWrite(config.getDuration("expireAfterWrite"));
            LOGGER.info("expireAfterWrite:" + config.getDuration("expireAfterWrite", TimeUnit.SECONDS));
        }
        if (config.hasPath("expireAfterAccess")) {
            builder.expireAfterAccess(config.getDuration("expireAfterAccess"));
            LOGGER.info("expireAfterAccess:" + config.getDuration("expireAfterAccess", TimeUnit.SECONDS));
        }
        if (config.hasPath("refreshAfterWrite")) {
            builder.refreshAfterWrite(config.getDuration("refreshAfterWrite"));
            LOGGER.info("refreshAfterWrite:" + config.getDuration("refreshAfterWrite", TimeUnit.SECONDS));
        }
        //缓存的最大条数,不是内存空间
        if (config.hasPath("maximumSize"))
            builder.maximumSize(config.getLong("maximumSize"));
        if (config.hasPath("capacity"))
            builder.initialCapacity(config.getInt("capacity"));
        if (config.hasPath("recordStats")) {
            stats = new CacheStats();
            builder.recordStats();
        }
        cache = builder.build();
    }

    @Override
    public void put(K key, V value) {
        if (key != null)
            cache.put(key, value);
    }

    @Override
    public void put(Map<? extends K, ? extends V> map) {
        cache.putAll(map);
    }

    @Override
    public V get(K key) {
        if (key == null)
            return null;
        return cache.getIfPresent(key);
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> function) {
        if (key == null)
            return null;
        return cache.get(key, function);
    }

    @SafeVarargs
    @Override
    public final Map<K, V> get(K... keys) {
        if (keys == null)
            return null;
        return cache.getAllPresent(Arrays.asList(keys));
    }

    @Override
    public Map<K, V> get(Iterable<? extends K> keys) {
        if (keys == null)
            return null;
        return cache.getAllPresent(keys);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<K, V> get(@NonNull Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
        return cache.getAll(keys, (Function<Iterable<? extends K>, Map<K, V>>) mappingFunction);
    }

    @Override
    public void evict(K key) {
        cache.invalidate(key);
    }

    @SafeVarargs
    @Override
    public final void evict(K... keys) {
        cache.invalidateAll(Arrays.asList(keys));
    }

    @Override
    public void evict(Iterable<? extends K> keys) {
        cache.invalidateAll(keys);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CaffeineCache.class.getSimpleName() + "[", "]")
                .add("cache=" + cache)
                .add("stats=" + stats)
                .toString();
    }
}
