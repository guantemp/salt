/*
 * Copyright (c) 2023. www.hoprxi.com All Rights Reserved.
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
 * @version 0.0.1 builder 2022-06-30
 */
public class CaffeineCache<K, V> implements Cache<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaffeineCache.class);
    private final com.github.benmanes.caffeine.cache.Cache<K, V> cache;
    private CacheStats stats = CacheStats.EMPTY_STATS;

    /**
     * @param config
     */
    public CaffeineCache(Config config) {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder();
        if (config.hasPath("expire")) {
            caffeine.expireAfterWrite(config.getDuration("expire"));
            LOGGER.info("expire:" + config.getDuration("expire", TimeUnit.SECONDS));
            //System.out.println("expire:" + config.getDuration("expire", TimeUnit.SECONDS));
        }
        //缓存的最大条数,不是内存空间
        if (config.hasPath("maximumSize"))
            caffeine.maximumSize(config.getLong("maximumSize"));
        if (config.hasPath("requireStats")) {
            stats = new CacheStats();
            caffeine.recordStats();
        }
        cache = caffeine.build();
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
        if (keys == null)
            return null;
        return cache.getAll(keys, (Function<Iterable<? extends K>, Map<K, V>>) mappingFunction);
    }

    @Override
    public void evict(K key) {
        cache.invalidate(key);
    }

    @Override
    public void evict(K... keys) {
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
