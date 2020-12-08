/*
 * Copyright (c) 2020. www.hoprxi.com All Rights Reserved.
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
package salt.hoprxi.cache.l1.concurrentMap;

import com.carrotsearch.sizeof.RamUsageEstimator;
import salt.hoprxi.cache.Cache;
import salt.hoprxi.cache.CacheStats;

import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.3 2020-03-14
 * @since JDK8.0
 */

public class ConcurrentMapCache<K, V> implements Cache<K, V> {
    private static final float LOAD_FACTOR = 0.95f;
    private final ConcurrentMap<K, V> memory;
    private final ExpiryPolicy<K> expiryPolicy;
    private final ExpireRecovery<K> recovery = new ExpireRecovery<>();
    private final String region;
    // cache stats
    private final CacheStats stats;
    // Initialize max cache capacity.default is 2048
    private long capacity = 1 << 11;
    // Of milliseconds,-1 never expire,
    private int expired = -1;

    /**
     * The ConcurrentMap the keys and values are stored in. Our primary data
     * structure is a HashMap.
     */
    public ConcurrentMapCache(String region) {
        this.region = Objects.requireNonNull(region, "region required").trim();
        memory = new ConcurrentHashMap<K, V>();
        stats = new CacheStats(-1, -1l, -1);
        expiryPolicy = new LRU<K>();
    }

    /**
     * Create a new cache and specify the maximum size of for the cache in
     * bytes, and the maximum lifetime of objects and the number of objects.
     * <p>
     * The ConcurrentMap the keys and values are stored in. Our primary data
     * structure is a HashMap.
     *
     * @param builder
     */
    ConcurrentMapCache(ConcurrentMapCacheBuilder builder) {
        region = builder.region();
        memory = new ConcurrentHashMap<K, V>();
        expiryPolicy = builder.expiryPolicy();
        stats = new CacheStats(builder.maxAmount(), builder.maximumSize(), builder.expired());
        if (builder.expired() != -1) {
            ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1, r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            });
            scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
            scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            scheduler.scheduleWithFixedDelay(() -> {
                evictExpiredEntries();
                cullCache();
            }, builder.expired(), builder.expired(), TimeUnit.MICROSECONDS);
        }
    }

    /**
     * application evict strategy
     */
    private void cullCache() {
        long maxCacheSize = stats.maxCacheSize();
        // Check if a max cache size is defined.
        if (maxCacheSize == -1) {
            return;
        }
        // See if the size of the cache exceeds the load factor definition size.
        // If so, clear the cache until it meets the size of the load factor definition.
        long desiredSize = (long) (maxCacheSize * LOAD_FACTOR);
        while (stats.currentSize() > desiredSize) {
            // Get the id and invoke the remove method on it.
            evict(expiryPolicy.peekLast());
        }
    }

    /**
     * Remove all old entries. To do this, we remove objects from the end of the
     * linked list until they are no longer too old. We get to avoid any hash
     * lookups or looking at any more objects than is strictly necessary.
     */
    private void evictExpiredEntries() {
        long timeout = stats.timeout();
        if (timeout == -1) {
            return;
        }
        // which is the moment in time that elements should expired from cache.
        // Then, we can do an easy to check to see if the expired time is greater than the expired time.
        for (ExpireRecovery.ExpiredBean<K> key = recovery.peekLast(); key != null && System.currentTimeMillis() - key.timestamp > timeout; key = recovery.peekLast()) {
            //evict from expired recovery
            recovery.remove(key.id);
            //evict from memory
            evict(key.id);
        }
    }

    @Override
    public void clear() {
        memory.clear();
        expiryPolicy.clear();
        recovery.clear();
        stats.clear();
    }

    @Override
    public V evict(K key) {
        V value = memory.remove(key);
        // removed the object
        expiryPolicy.remove(key);
        recovery.remove(key);
        //chang stats state
        stats.reduceCurrentSize(RamUsageEstimator.sizeOf(value)).increaseEviction().reduceCurrentAmount();
        return value;
    }

    @Override
    public void evict(K... keys) {

    }

    @Override
    public V get(K key) {
        V value = memory.get(key);
        if (value == null) {//Key didn't exist in cache, increment cache misses.
            stats.increaseMisses();
        } else {// Key exists in cache, so increment cache hits and update access
            stats.increaseHits();
            expiryPolicy.offerFirst(key);
            recovery.remove(key);
            recovery.offerFirst(key);
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
            return memory.get(key);
        }
        // If the object is bigger than the entire cache,  do nothing
        long size = RamUsageEstimator.sizeOf(value);
        long maxCacheSize = stats.maxCacheSize();
        if (maxCacheSize > 0 && size > maxCacheSize * LOAD_FACTOR) {
            return memory.get(key);
        }
        V old = memory.put(key, value);
        if (old != null)
            stats.reduceCurrentAmount().reduceCurrentSize(RamUsageEstimator.sizeOf(old));
        // Add the id to the cull and expired list
        expiryPolicy.offerFirst(key);
        recovery.offerFirst(key);
        // update cache stats
        stats.increaseCurrentAmount().increaseCurrentSize(size);
        return old;
    }

    private final static class ExpireRecovery<K> extends ReentrantReadWriteLock {
        LinkedList<ExpiredBean<K>> list = new LinkedList<>();

        /**
         * clear all data
         */
        void clear() {
            writeLock().lock();
            try {
                list.clear();
            } finally {
                writeLock().unlock();
            }
        }

        /**
         * @param key
         * @return
         */
        boolean offerFirst(K key) {
            writeLock().lock();
            try {
                ExpiredBean<K> bean = new ExpiredBean(key);
                list.remove(bean);
                return list.offerFirst(bean);
            } finally {
                writeLock().unlock();
            }
        }

        /**
         * @param key
         * @return
         */
        boolean remove(K key) {
            writeLock().lock();
            try {
                return list.remove(new ExpiredBean(key));
            } finally {
                writeLock().unlock();
            }
        }

        /**
         * @return
         */
        ExpiredBean<K> peekLast() {
            readLock().lock();
            try {
                ExpiredBean<K> key = list.peekLast();
                return key == null ? null : key;
            } finally {
                readLock().unlock();
            }
        }

        /**
         * This class is further customized for the CoolServlets cache system.
         * It maintains a id and timestamp of when a Cacheable object was first
         * added to cache. Timestamps are stored as long values and represent
         * the number of milleseconds passed since January 1, 1970 08:00:00.000
         * GMT.
         */
        private final static class ExpiredBean<K> {
            final K id;
            /**
             * The creation timestamp is used in the case that the cache has a
             * maximum lifetime set. In that case, when [current time] -
             * [creation time] > [max lifetime], the object will be deleted from
             * cache.
             */
            final long timestamp;

            ExpiredBean(K id) {
                this.id = Objects.requireNonNull(id, "required id");
                timestamp = System.currentTimeMillis();
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                ExpiredBean<?> that = (ExpiredBean<?>) o;

                return id.equals(that.id);
            }

            @Override
            public int hashCode() {
                return id.hashCode();
            }
        }
    }
}
