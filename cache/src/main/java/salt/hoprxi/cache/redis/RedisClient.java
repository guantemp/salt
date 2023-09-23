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
package salt.hoprxi.cache.redis;


import java.io.Closeable;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.2 2022-08-01
 * @since JDK8.0
 */
public interface RedisClient<K, V> extends Closeable, AutoCloseable {
    /**
     * @param key
     * @param value
     */
    void set(K key, V value);

    /**
     * @param key
     * @param value
     */
    void hset(K key, V value);

    /**
     * @param map
     */
    void set(Map<? extends K, ? extends V> map);

    /**
     * @param key
     * @return
     */
    V get(K key);

    /**
     * @param key
     * @param function
     * @return
     */
    V get(K key, Function<? super K, ? extends V> function);

    /**
     * @param key
     * @return
     */
    V hget(K key);


    /**
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
    Map<K, V> get(Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction);

    /**
     * @param key
     */
    void hdel(K key);

    /**
     * @param key
     */
    void del(K key);

    /**
     * @param keys
     */
    void del(K... keys);

    /**
     * @param keys
     */
    void del(Iterable<? extends K> keys);


    /**
     * clear all cache data
     */
    void clear();
}
