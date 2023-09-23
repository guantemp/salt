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

package salt.hoprxi.cache;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.4 2022-07-28
 * @since JDK8.0
 */
public interface Cache<K, V> {
    /**
     * Associates {@code value} with {@code key} in this cache.
     * If the cache previously contained a value associated with {@code key}, the old value is replaced by {@code value}
     * If {@code value} size bigger than (maximumSize),do nothing
     * If amount bigger than maxAmount or size bigger than maximumSize,the oldest data will be evict whether the life cycle is reached or not
     *
     * @param key   the key
     * @param value the value
     * @throws NullPointerException if the specified key is null or the
     *                              value is null and the cache does not permit null values
     */
    void put(K key, V value);

    /**
     * @param map the mappings to be stored in this cache
     * @throws NullPointerException if the specified map is null or the specified map contains null keys or values
     */
    void put(Map<? extends K, ? extends V> map);

    /**
     * Returns the value associated with {@code key} in this cache, or
     * {@code null} if there is no cached value for {@code key}.
     *
     * @param key the key
     * @return value in cache,<code>null</code> if not fin
     * @throws NullPointerException if the specified key is null
     */
    V get(K key);

    /**
     * Returns the callable associated with {@code key} in this cache
     * When the object is not in the cache or contain expired (not yet recycled), the computed value is returned
     *
     * @param key      the key with which the specified value is to be associated
     * @param function the function to compute a value
     * @return the current (existing or computed) value associated with the specified key, or null if
     * the computed value is null
     * @throws NullPointerException if the specified key or mappingFunction is null
     */
    V get(K key, Function<? super K, ? extends V> function);

    /**
     * Returns the values of all (one or more) given keys. If a value does not exist in a given key, the value is reject
     *
     * @param keys
     * @return
     */
    Map<K, V> get(K... keys);

    /**
     * @param keys
     * @return
     */
    Map<K, V> get(Iterable<? extends K> keys);

    /**
     * @param keys
     * @param mappingFunction
     * @return
     */
    Map<K, V> get(Iterable<? extends K> keys,
                  Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction);


    /**
     * Discards any cached value for key {@code key}.
     *
     * @param key the key
     */
    void evict(K key);

    /**
     * @param keys
     */
    void evict(K... keys);

    /**
     * @param keys
     */
    void evict(Iterable<? extends K> keys);

    /**
     * Discards all entries in the cache.
     */
    void clear();
}
