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

import java.util.Objects;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 2020-03-21
 */
public class CacheEvent<K, V> {
    private K key;
    private V value;
    private String region;
    private CacheOperation operation;

    public CacheEvent(CacheOperation operation, String region, K key, V value) {
        this.key = key;
        this.value = value;
        this.region = region;
        setOperation(operation);
    }

    private void setOperation(CacheOperation operation) {
        this.operation = Objects.requireNonNull(operation, "operation required");
    }

    public K key() {
        return key;
    }

    public V value() {
        return value;
    }

    public String region() {
        return region;
    }

    public CacheOperation operation() {
        return operation;
    }
}
