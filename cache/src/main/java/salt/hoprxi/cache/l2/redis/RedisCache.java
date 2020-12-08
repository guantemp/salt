/*
 * Copyright (c) 2020 www.hoprxi.com All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package salt.hoprxi.cache.l2.redis;

import com.carrotsearch.sizeof.RamUsageEstimator;
import salt.hoprxi.cache.Cache;
import salt.hoprxi.cache.CacheStats;
import salt.hoprxi.cache.l2.redis.jedis.JedisClient;

import java.util.concurrent.Callable;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.2 2019-03-18
 * @since JDK8.0
 */
public class RedisCache<K, V> implements Cache<K, V> {
    private static final float LOAD_FACTOR = 0.95f;
    private final JedisClient<K, V> client;
    private final CacheStats stats;
    private final String region;

    RedisCache(RedisCacheBuilder<K, V> builder) {
        region = builder.region();
        client = builder.client();
        stats = new CacheStats(builder.maxAmount(), builder.maximumSize(), builder.expired());
    }

    /**
     * Discards all entries in the cache.
     */
    @Override
    public void clear() {
        client.clear(region);
        stats.clear();
    }

    @Override
    public V get(K key) {
        V value = client.get(key, region);
        if (value == null) {
            stats.increaseMisses();
        } else {
            stats.increaseHits();
        }
        return value;
    }

    @Override
    public V get(K key, Callable<? extends V> value) {
        return null;
    }

    @Override
    public V[] get(K... keys) {
        return null;
    }

    @Override
    public V put(K key, V value) {
        // If amount bigger than the entire cache amount, do nothing.
        long maxAmount = stats.maxAmount();
        long amount = stats.currentAmount();
        if (maxAmount > 0 && amount + 1 > maxAmount) {
            return client.get(key, region);
        }
        // If the object is bigger than the entire cache,  do nothing
        long size = RamUsageEstimator.sizeOf(value);
        long maxCacheSize = stats.maxCacheSize();
        if (maxCacheSize > 0 && size > maxCacheSize * LOAD_FACTOR) {
            return client.get(key, region);
        }
        V old = client.put(key, region, value);
        if (old != null) {
            stats.reduceCurrentAmount().reduceCurrentSize(RamUsageEstimator.sizeOf(old));
        }
        stats.increaseCurrentAmount().increaseCurrentSize(size);
        return old;
    }

    @Override
    public V evict(K key) {
        V old = client.remove(key, region);
        //chang stats state
        stats.reduceCurrentSize(RamUsageEstimator.sizeOf(old)).increaseEviction().reduceCurrentAmount();
        return old;
    }

    @Override
    public void evict(K... keys) {

    }
}
