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

package salt.hoprxi.cache;

import java.util.concurrent.Callable;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.1 2020-09-13
 * @since JDK8.0
 */
public interface Cache<K, V> {
    /**
     * Associates {@code value} with {@code key} in this cache.
     * If the cache previously contained a value associated with {@code key}, the old value is replaced by {@code value}.and return
     * If {@code value} size bigger than (maxCacheSize*loadFactor),do nothing
     * If amount bigger than maxAmount,do nothing
     *
     * @param key   the key
     * @param value the value
     * @return old value
     * @throws NullPointerException if the specified key is null or the
     *                              value is null and the cache does not permit null values
     */
    V put(K key, V value);

    /**
     * Associates {@code value} with {@code key} in this cache expire time. If
     * the cache previously contained a value associated with {@code key}, the
     * old value is replaced by {@code value}and and return
     * throws UnsupportedOperationException if unsupported
     *
     * @param key   the key
     * @param value the value
     * @param ttl   in milliseconds
     * @return old value
     * @throws UnsupportedOperationException if not support timeout
     * @throws NullPointerException          if the specified key is null or the
     *                                       value is null and the cache does not permit null values
     */
    default V put(K key, V value, long ttl) {
        throw new UnsupportedOperationException("not support");
    }

    /**
     * Returns {@code true}, if has value for the specified key.
     *
     * <p>Effect on statistics: The operation does increase the usage counter if a mapping is present,
     * but does not count as read and therefore does not influence miss or hit values.</p>
     *
     * @param key
     * @return
     * @throws NullPointerException if the specified key is null
     */
    default boolean isContain(K key) {
        return get(key) != null;
    }

    /**
     * Returns the value associated with {@code key} in this cache, or
     * {@code null} if there is no cached value for {@code key}.
     *
     * @param key the key
     * @return value in cache,<code>null</code> if not find
     */
    V get(K key);

    /**
     * Returns the value associated with {@code key} in this cache,if value isn't exists,
     *
     * @param key
     * @param value
     * @return
     */
    V get(K key, Callable<? extends V> value);

    /**
     * Returns the values of all (one or more) given keys. If a key does not exist in a given key, the key returns null
     *
     * @param keys
     * @return
     */
    V[] get(K... keys);


    /**
     * Discards any cached value for key {@code key}.
     *
     * @param key the key
     * @return value in cache,or <code>null</code> If a key does not exist in a given key
     */
    V evict(K key);

    /**
     * @param keys
     */
    void evict(K... keys);

    /**
     * Discards all entries in the cache.
     */
    void clear();
}
