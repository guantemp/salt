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
package salt.hoprxi.cache.l1_2;

import salt.hoprxi.cache.Cache;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author <a href="www.hoprxi.com/author/guan xianHuang">guan xiangHuan</a>
 * @version 0.0.3 2022-08-03
 * @since JDK8.0
 */
public class L1_2Cache<K, V> implements Cache<K, V> {
    private final Cache<K, V> local;
    private final Cache<K, V> remote;
    //private StampedLock lock

    /**
     * @param local  level 1 cache,is l1 cache
     * @param remote level 1 cache,is remote cache
     */
    public L1_2Cache(Cache<K, V> local, Cache<K, V> remote) {
        this.local = Objects.requireNonNull(local, "local cache is required");
        this.remote = Objects.requireNonNull(remote, "remote cache is required");
    }

    @Override
    public void put(K key, V value) {
        local.put(key, value);
        remote.put(key, value);
    }

    @Override
    public void put(Map<? extends K, ? extends V> map) {
        local.put(map);
        remote.put(map);
    }

    @Override
    public V get(K key) {
        V result = local.get(key);
        if (result != null)
            return result;
        result = remote.get(key);
        if (result != null) {
            local.put(key, result);
        }
        return result;
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> function) {
        V result = local.get(key);
        if (result != null)
            return result;
        //  result = Optional.ofNullable(remote.value(key)).orElseGet(() -> function.apply(key));
        result = remote.get(key);
        if (result == null) {
            result = function.apply(key);
        }
        if (result != null) {
            local.put(key, result);
            remote.put(key, result);
        }
        return result;
    }

    @Override
    public Map<K, V> get(K... keys) {
        return get(Arrays.asList(keys));
    }

    @Override
    public Map<K, V> get(Iterable<? extends K> keys) {
        Map<K, V> localMap = local.get(keys);
        Set<K> remoteKeys = StreamSupport.stream(keys.spliterator(), true).filter(k -> !localMap.containsKey(k)).collect(Collectors.toSet());
        if (remoteKeys.size() == 0)
            return Collections.unmodifiableMap(localMap);
        Map<K, V> remoteMap = remote.get(remoteKeys);
        Map<K, V> result = Stream.of(localMap, remoteMap)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v2));
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<K, V> get(Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
        Map<K, V> before = get(keys);
        Set<K> set = StreamSupport.stream(keys.spliterator(), true).filter(k -> !before.containsKey(k)).collect(Collectors.toSet());
        if (before.size() == 0)
            return Collections.unmodifiableMap(before);
        Map<K, V> remainder = (Map<K, V>) mappingFunction.apply(set);
        local.put(remainder);
        remote.put(remainder);
        Map<K, V> result = Stream.of(before, remainder)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v2));
        return Collections.unmodifiableMap(result);
    }


    @Override
    public void evict(K key) {
        local.evict(key);
        remote.evict(key);
    }

    @Override
    public void evict(K... keys) {
        local.evict(keys);
        remote.evict(keys);

    }

    @Override
    public void evict(Iterable<? extends K> keys) {
        local.evict(keys);
        remote.evict(keys);
    }

    @Override
    public void clear() {
        local.clear();
        remote.clear();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", L1_2Cache.class.getSimpleName() + "[", "]")
                .add("local=" + local)
                .add("remote=" + remote)
                .toString();
    }
}
