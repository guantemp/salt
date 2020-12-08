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
package salt.hoprxi.cache.l1_2;

import salt.hoprxi.cache.Cache;
import salt.hoprxi.cache.CacheStats;
import salt.hoprxi.cache.l1.concurrentMap.ConcurrentMapCache;
import salt.hoprxi.cache.l2.redis.RedisCache;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author <a href="www.hoprxi.com/author/guan xianHuang">guan xiangHuan</a>
 * @version 0.0.2 2019-03-20
 * @since JDK8.0
 */
public class L1_2Cache<K, V> implements Cache<K, V> {
    private final Cache<K, V> l1;
    private final Cache<K, V> l2;
    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * @param l1 level 1 cache,is l1 cache
     * @param l2 level 1 cache,is remote cache
     */
    public L1_2Cache(Cache<K, V> l1, Cache<K, V> l2) {
        l1 = Objects.requireNonNull(l1, "L1 cache is required");
        if (!(l1 instanceof ConcurrentMapCache))
            throw new IllegalArgumentException("l1 isn't level 1 cache");
        this.l1 = l1;
        l2 = Objects.requireNonNull(l2, "l2 cache is required");
        if (!(l2 instanceof RedisCache))
            throw new IllegalArgumentException("l2 isn't level 2 cache");
        this.l2 = l2;
    }

    @Override
    public void clear() {
        readWriteLock.writeLock().lock();
        try {
            l1.clear();
            l2.clear();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public V evict(K key) {
        readWriteLock.writeLock().lock();
        V old = null;
        try {
            old = l1.evict(key);
            old = l2.evict(key);
        } finally {
            readWriteLock.writeLock().unlock();
        }
        return old;
    }

    @Override
    public void evict(K... keys) {

    }

    @Override
    public V get(K key) {
        readWriteLock.readLock().lock();
        try {
            V value = l1.get(key);
            if (null == value) {
                value = l2.get(key);
                if (null != value) {
                    readWriteLock.readLock().unlock();
                    readWriteLock.writeLock().lock();
                    try {
                        l1.put(key, value);
                    } finally {
                        readWriteLock.writeLock().unlock();
                    }
                }
                readWriteLock.readLock().lock();
            }
            return value;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public V get(K key, Callable<? extends V> value) {
        return null;
    }

    @Override
    public V[] get(K... keys) {
        return null;
    }

    /**
     * @return First cache stats
     */
    public CacheStats l1_stats() {
        return null;
    }

    /**
     * @return Second cache stats
     */
    public CacheStats l2_stats() {
        return null;
    }

    @Override
    public V put(K key, V value) {
        readWriteLock.writeLock().lock();
        try {
            V old1 = l1.put(key, value);
            V old2 = l2.put(key, value);
            return old1 != null ? old1 : old2 != null ? old2 : null;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public V put(K key, V value, long timeout) {
        readWriteLock.writeLock().lock();
        try {
            V old1 = l1.put(key, value);
            V old2 = l2.put(key, value, timeout);
            return old1 != null ? old1 : old2 != null ? old2 : null;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}
