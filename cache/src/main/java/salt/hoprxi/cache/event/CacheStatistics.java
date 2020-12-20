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

package salt.hoprxi.cache.event;


/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 2020-03-15
 */
public class CacheStatistics {
    private int currentAmount = 0;
    private long eviction = 0l;
    private long hits = 0l;
    private long misses = 0l;
    private int currentCapacity = 0;

    public void clear() {
        currentAmount = 0;
        eviction = 0l;
        hits = 0l;
        misses = 0l;
        currentCapacity = 0;
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
     * @return number of cache remove
     */
    public long eviction() {
        return eviction;
    }

    /**
     * Returns the number of cache hits. A cache hit occurs every time the get
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
     * Returns the number of cache misses. A cache miss occurs every time the
     * get method is called and the cache does not contain the requested object.
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
    public long currentCapacity() {
        return currentCapacity;
    }
}
