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

package salt.hoprxi.cache;


import salt.hoprxi.cache.event.CacheStatistics;
import salt.hoprxi.cache.l1.concurrentMap.ConcurrentMapCacheProvider;


/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.1 2020-03-21
 */
public class CacheManager {
    private static ConcurrentMapCacheProvider concurrentCacheProvider = new ConcurrentMapCacheProvider();

    public static <K, V> Cache<K, V> buildCache(String region) {
        return concurrentCacheProvider.buildCache(region);
    }

    public static void stop() {
    }

    CacheStatistics statistics(String region) {
        return null;
    }
}
