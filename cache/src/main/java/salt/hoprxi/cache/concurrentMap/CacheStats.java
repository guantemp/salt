/*
 * Copyright (c) 2022. www.hoprxi.com All Rights Reserved.
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

import java.util.StringJoiner;

/**
 * @author <a href="www.hoprxi.com/author/guan xiangHuan">guan xiangHuan</a>
 * @version 0.0.2 2019-04-01
 * @since JDK8.0
 */
@Deprecated
public final class CacheStats {
    // number of element
    private long currentAmount = 0L;
    private long eviction = 0L;
    private long hits = 0L;
    // max number of element,-1 if not limit
    private int maxAmount = -1;
    // Initialize max cache currentSize.-1 not limit
    private long maximumSize = -1L;
    private long misses = 0L;
    private long currentSize = 0L;
    // Of milliseconds,-1 never expire,
    private int expired = -1;


    /**
     * @param maxAmount
     * @param maximumSize
     * @param expired     of milliseconds
     */
    public CacheStats(int maxAmount, long maximumSize, int expired) {
        setMaxAmount(maxAmount);
        setMaximumSize(maximumSize);
        setExpired(expired);
    }

    // Initialize the cache statistics
    public void clear() {
        currentAmount = 0L;
        eviction = 0L;
        hits = 0L;
        misses = 0L;
        currentSize = 0L;
    }

    /**
     * Returns the element number of the cache
     *
     * @return number of element.
     */
    public long currentAmount() {
        return currentAmount;
    }

    /**
     * @return number of cache del
     */
    public long eviction() {
        return eviction;
    }

    //@Override
    public void onElementEvicted(Object key, Object value) {
        eviction++;
        //currentSize -= size;
    }

    /**
     * Returns the number of cache hits. A cache hit occurs every time the hget
     * method is called and the cache contains the requested object.
     * <p>
     * <p>
     * Keeping track of cache hits and misses lets one measure how efficient the
     * cache is; the higher the percentage of hits, the more efficient.
     *
     * @return the number of cache hits.
     */
    public long hits() {
        return hits;
    }

    /**
     * @return the maxAmount
     */
    public long maxAmount() {
        return maxAmount;
    }

    /**
     * Returns the maximum currentSize of the cache in bytes. If the cache grows larger
     * than the max currentSize, the least frequently used items will be removed. If
     * the max cache currentSize is set to -1, there is no currentSize limit.
     *
     * @return the maximum currentSize of the cache in bytes.
     */
    public long maxCacheSize() {
        return maximumSize;
    }

    /**
     * Returns the number of cache misses. A cache miss occurs every time the
     * hget method is called and the cache does not contain the requested object.
     * <p>
     * <p>
     * Keeping track of cache hits and misses lets one measure how efficient the
     * cache is; the higher the percentage of hits, the more efficient.
     *
     * @return the number of cache misses.
     */
    public long misses() {
        return misses;
    }

    /**
     * @return the currentSize
     */
    public long currentSize() {
        return currentSize;
    }

    public long timeout() {
        return expired;
    }

    /**
     * Returns the maximum number of milliseconds that any object can live in
     * cache. Once the specified number of milliseconds passes, the object will
     * be automatically expried from cache. If the lifetime is set to -1, then
     * objects never expire.
     *
     * @return the maximum number of milliseconds before objects are expire.
     */
    public int expired() {
        return expired;
    }

    public CacheStats increaseCurrentAmount() {
        currentAmount++;
        return this;
    }

    public CacheStats reduceCurrentAmount() {
        currentAmount--;
        return this;
    }

    public CacheStats increaseEviction() {
        eviction++;
        return this;
    }

    public CacheStats increaseHits() {
        hits++;
        return this;
    }

    public CacheStats increaseMisses() {
        misses++;
        return this;
    }

    /**
     * @param size
     */
    public CacheStats increaseCurrentSize(long size) {
        this.currentSize += size;
        return this;
    }

    /**
     * @param size
     */
    public CacheStats reduceCurrentSize(long size) {
        this.currentSize -= size;
        return this;
    }


    /**
     * Maximum cache currentSize adjustment
     *
     * @param cacheSize Increase the max currentSize, if is negative will reduce the max currentSize
     * @return
     */
    public CacheStats adjustMaxSize(long cacheSize) {
        this.maximumSize += cacheSize;
        return this;
    }


    /**
     * Adjustment max currentAmount
     *
     * @param maxAmount Increase the max currentAmount, if is negative will reduce the max currentAmount
     * @return
     */
    public CacheStats adjustMaxAmount(long maxAmount) {
        this.maxAmount += maxAmount;
        return this;
    }


    /**
     * @param maxAmount the maxAmount to set
     */
    private void setMaxAmount(int maxAmount) {
        if (maxAmount > 0)
            this.maxAmount = maxAmount;
    }

    /**
     * Sets the maximum currentSize of the cache in bytes. If the cache grows larger
     * than the max currentSize, the least frequently used items will be removed. If
     * the max cache currentSize is set to -1, there is no currentSize limit.
     *
     * @param maximumSize the maximum currentSize of the cache in bytes.
     */
    private void setMaximumSize(long maximumSize) {
        if (maximumSize > 0)
            this.maximumSize = maximumSize;
    }

    /**
     * Sets the maximum number of milliseconds that any object can live in
     * cache. Once the specified number of milliseconds passes, the object will
     * be automatically expried from cache. If the lifetime is set to -1, then
     * objects never expire.
     *
     * @param expired the maximum number of milliseconds before objects are expire.
     */
    private void setExpired(int expired) {
        if (expired > 0)
            this.expired = expired;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CacheStats.class.getSimpleName() + "[", "]")
                .add("currentAmount=" + currentAmount)
                .add("eviction=" + eviction)
                .add("hits=" + hits)
                .add("maxAmount=" + maxAmount)
                .add("maximumSize=" + maximumSize)
                .add("misses=" + misses)
                .add("currentSize=" + currentSize)
                .add("expired=" + expired)
                .toString();
    }
}