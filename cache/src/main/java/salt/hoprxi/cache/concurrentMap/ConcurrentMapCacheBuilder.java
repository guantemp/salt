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

package salt.hoprxi.cache.concurrentMap;


import salt.hoprxi.cache.Cache;
import salt.hoprxi.utils.Builder;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.1 2019-03-21
 */
@Deprecated
public class ConcurrentMapCacheBuilder<K, V> implements Builder<Cache<K, V>> {
    private ExpiryPolicy<K> expiryPolicy = new LRU<K>();
    private int capacity = 1 << 11;
    private int maxAmount = -1;
    private final String region;
    private long maximumSize = -1L;
    private int expired = -1;

    public ConcurrentMapCacheBuilder(String region) {
        this.region = Objects.requireNonNull(region, "required region").trim();
    }

    public int maxAmount() {
        return maxAmount;
    }

    public long maximumSize() {
        return maximumSize;
    }

    public int expired() {
        return expired;
    }

    @Override
    public Cache<K, V> build() {
        return new ConcurrentMapCache(this);
    }

    /**
     * @param maxAmount of cache,not limit if -1
     * @return
     */
    public ConcurrentMapCacheBuilder<K, V> maxAmount(int maxAmount) {
        if (maxAmount > 0)
            this.maxAmount = maxAmount;
        return this;
    }

    /**
     * @param maximumSize of cache,not limit if negative
     * @return
     */
    public ConcurrentMapCacheBuilder<K, V> maximumSize(long maximumSize) {
        if (maximumSize > 0)
            this.maximumSize = maximumSize;
        return this;
    }

    /**
     * @param expiryPolicy
     * @return
     */
    public ConcurrentMapCacheBuilder<K, V> expiryPolicy(ExpiryPolicy<K> expiryPolicy) {
        this.expiryPolicy = Objects.requireNonNull(expiryPolicy, "Cull ploy required");
        return this;
    }

    /**
     * @param capacity
     * @return
     */
    public ConcurrentMapCacheBuilder<K, V> capacity(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException("capacity is too samll");
        this.capacity = capacity;
        return this;
    }

    /**
     * Expired builder.
     *
     * @param expired the expired
     * @param unit    the unit
     * @return the builder
     */
    public ConcurrentMapCacheBuilder<K, V> expired(int expired, TimeUnit unit) {
        if (expired >= -1) {
            switch (unit) {
                case HOURS:
                    this.expired = expired * 60 * 60 * 1000;
                    break;
                case MINUTES:
                    this.expired = expired * 60 * 1000;
                    break;
                case SECONDS:
                    this.expired = expired * 1000;
                    break;
                default:
                    this.expired = expired;

            }
        }
        return this;
    }

    public ExpiryPolicy<K> expiryPolicy() {
        return expiryPolicy;
    }

    public String region() {
        return region;
    }

    public int capacity() {
        return capacity;
    }
}
