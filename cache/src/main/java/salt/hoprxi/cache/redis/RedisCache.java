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
package salt.hoprxi.cache.redis;

import salt.hoprxi.cache.Cache;
import salt.hoprxi.cache.event.CacheStats;

import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.2 2019-03-18
 * @since JDK21
 */
public class RedisCache<K, V> implements Cache<K, V> {
    private final RedisClient<K, V> client;
    private final CacheStats stats;
    private final String region;

    protected RedisCache(RedisCacheBuilder<K, V> builder) {
        region = builder.region();
        client = builder.client();
        stats = new CacheStats(builder.maxAmount(), builder.maximumSize());
    }

    public RedisCache(String region, RedisClient client) {
        this.region = region;
        this.client = client;
        stats = CacheStats.EMPTY_STATS;
    }

    /**
     * Discards all entries in the cache.
     */
    @Override
    public void clear() {
        client.clear();
        stats.reset();
    }

    @Override
    public V get(K key) {
        V value = client.get(key);
        if (value == null) {
            stats.increaseMisses();
        } else {
            stats.increaseHits();
        }
        return value;
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> function) {
        return client.get(key, function);
    }

    @Override
    public Map<K, V> get(K... keys) {
        return client.get(keys);
    }

    @Override
    public Map<K, V> get(Iterable<? extends K> keys) {
        return client.get(keys);
    }

    @Override
    public Map<K, V> get(Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
        return client.get(keys, mappingFunction);
    }

    @Override
    public void put(K key, V value) {
        // If amount bigger than the entire cache amount, do nothing.
        /*
        long maxAmount = stats.maxAmount();
        long amount = stats.currentAmount();
        if (maxAmount > 0 && amount + 1 > maxAmount) {
            return;
        }*/
        // If the object is bigger than the entire cache,  do nothing
        //long size = RamUsageEstimator.sizeOf(value);
        /*
        long maxCacheSize = stats.maxCacheSize();
        if (maxCacheSize > 0 && size > maxCacheSize * LOAD_FACTOR) {
            return;
        }
        */
        // V old = client.value(key);
        // if (old != null) {
        // stats.reduceCurrentAmount().reduceCurrentSize(RamUsageEstimator.sizeOf(old));
        //}
        //stats.increaseCurrentAmount().increaseCurrentSize(size);
        client.set(key, value);
    }

    @Override
    public void put(Map<? extends K, ? extends V> map) {
        client.set(map);
    }

    @Override
    public void evict(K key) {
        client.del(key);
    }

    @Override
    public void evict(K... keys) {
        client.del(keys);
    }

    @Override
    public void evict(Iterable<? extends K> keys) {
        client.del(keys);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RedisCache.class.getSimpleName() + "[", "]")
                .add("client=" + client)
                .add("stats=" + stats)
                .add("region='" + region + "'")
                .toString();
    }
}
