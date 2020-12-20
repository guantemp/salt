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

package salt.hoprxi.cache.l1.concurrentMap;

import salt.hoprxi.cache.Cache;
import salt.hoprxi.cache.CacheProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 2020-01-21
 */
public class ConcurrentMapCacheProvider implements CacheProvider {
    private static ConcurrentHashMap<String, Cache<?, ?>> caches = new ConcurrentHashMap<>();

    @Override
    public <K, V> Cache<K, V> buildCache(String region) {
        Cache<K, V> cache = (Cache<K, V>) caches.get(region);
        if (cache == null) {
            cache = new ConcurrentMapCacheBuilder<K, V>(region).expired(15, TimeUnit.MINUTES).build();
            caches.put(region, cache);
        }
        return cache;
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown(String region) {

    }
}
