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
import salt.hoprxi.utils.Builder;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.1 2021-04-02
 */
public class RedisCacheBuilder<K, V> implements Builder<Cache<K, V>> {
    private int maxAmount = -1;
    private final String region;
    private long maximumSize = -1L;
    private int expired = -1;
    private RedisClient client;

    public RedisCacheBuilder(String region) {
        this.region = Objects.requireNonNull(region, "region required").trim();
    }

    public RedisCacheBuilder(String region, RedisClient client) {
        this.region = Objects.requireNonNull(region, "region required").trim();
        this.client = Objects.requireNonNull(client, "client required");
    }

    public RedisCacheBuilder<K, V> client(RedisClient client) {
        this.client = Objects.requireNonNull(client, "client required");
        return this;
    }

    public RedisCacheBuilder<K, V> host(String host, int port) {
        return this;
    }

    /**
     * @param maxAmount of cache,not limit if -1
     * @return
     */
    public RedisCacheBuilder<K, V> maxAmount(int maxAmount) {
        if (maxAmount > 0)
            this.maxAmount = maxAmount;
        return this;
    }

    /**
     * @param maximumSize of cache,not limit if negative
     * @return
     */
    public RedisCacheBuilder<K, V> maximumSize(long maximumSize) {
        if (maximumSize > 0L)
            this.maximumSize = maximumSize;
        return this;
    }

    @Override
    public Cache<K, V> build() {
        return new RedisCache<>(this);
    }

    /**
     * @param expired
     * @param unit    support hours,minutes,senconds,milliseconds
     * @return
     */
    public RedisCacheBuilder<K, V> expired(int expired, TimeUnit unit) {
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

    /**
     * @param expired
     * @return
     */
    public RedisCacheBuilder<K, V> expired(int expired) {
        if (expired > 0)
            this.expired = expired;
        return this;
    }

    public int maxAmount() {
        return maxAmount;
    }

    public long maximumSize() {
        return maximumSize;
    }

    public String region() {
        return region;
    }

    public int expired() {
        return expired;
    }

    public RedisClient client() {
        return client;
    }
}
