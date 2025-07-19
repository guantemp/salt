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

package salt.hoprxi.cache.event;


/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.2 2022-07-02
 */
public final class CacheStats {
    public static final CacheStats EMPTY_STATS = new CacheStats(-1L, -1L);
    private long maximumCapacity = -1;
    private long maximumSize = -1;
    private int amount = 0;
    private long eviction = 0L;
    private long hits = 0L;
    private long misses = 0L;
    private final long loadSuccess = 0L;
    private final long loadFailure = 0L;
    private long memory = 0L;

    public CacheStats(long maximumCapacity, long maximumSize) {
        this.maximumCapacity = maximumCapacity;
        this.maximumSize = maximumSize;
    }

    public CacheStats() {
    }

    /**
     * @return
     */
    public long maximumCapacity() {
        return maximumCapacity;
    }

    /**
     * @return
     */
    public long maximumSize() {
        return maximumSize;
    }

    /**
     * @return
     */
    public long loadSuccess() {
        return loadSuccess;
    }

    /**
     * @return
     */
    public long loadFailure() {
        return loadFailure;
    }

    public void reset() {
        amount = 0;
        eviction = 0L;
        hits = 0L;
        misses = 0L;
        memory = 0L;
    }

    public void increaseHits() {
        hits += 1;
    }

    public void increaseMisses() {
        misses += 1;
    }

    public void increaseEviction() {
        eviction += 1;
    }

    public void increaseAmount() {
        amount += 1;
    }

    public void increaseMemory(int memory) {
        this.memory = this.memory + memory;
    }

    /**
     * Returns the element number of the cache
     *
     * @return number of element.
     */
    public long amount() {
        return amount;
    }

    /**
     * Returns the number of times an entry contain been evicted
     *
     * @return number of cache del
     */
    public long eviction() {
        return eviction;
    }

    /**
     * Returns the number of cache hits. A cache hit occurs every time the value
     * method is called and the cache contains the requested object.
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
     * Returns the number of cache misses. A cache miss occurs every time the
     * value method is called and the cache does not contain the requested object.
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
     * @return the byte length of currentSize
     */
    public long memory() {
        return memory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheStats)) return false;

        CacheStats that = (CacheStats) o;

        if (maximumCapacity != that.maximumCapacity) return false;
        if (maximumSize != that.maximumSize) return false;
        if (amount != that.amount) return false;
        if (eviction != that.eviction) return false;
        if (hits != that.hits) return false;
        if (misses != that.misses) return false;
        return memory == that.memory;
    }

    @Override
    public int hashCode() {
        int result = (int) (maximumCapacity ^ (maximumCapacity >>> 32));
        result = 31 * result + (int) (maximumSize ^ (maximumSize >>> 32));
        result = 31 * result + amount;
        result = 31 * result + (int) (eviction ^ (eviction >>> 32));
        result = 31 * result + (int) (hits ^ (hits >>> 32));
        result = 31 * result + (int) (misses ^ (misses >>> 32));
        result = 31 * result + (int) (loadSuccess ^ (loadSuccess >>> 32));
        result = 31 * result + (int) (loadFailure ^ (loadFailure >>> 32));
        result = 31 * result + (int) (memory ^ (memory >>> 32));
        return result;
    }
}
