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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2022-07-02
 */
public class CacheEventHandle {
    private static final ExecutorService executor;

    static {
        int processors = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(processors + 1);
    }

    private CacheEventHandle() {

    }

    public static CacheEventHandle getInstance() {
        return null;
    }

    public <K, V> void handle(CacheEvent<K, V> event, CacheStats stats) {
        switch (event.operation()) {
            case GET:
                stats.increaseAmount();
                break;
            case PUT:
                stats.increaseAmount();
                stats.increaseMemory(5);
                break;
            case EVICT:
                break;
            case CLEAR:
                break;
        }
    }
}
