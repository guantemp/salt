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
package salt.hoprxi.cache.concurrentMap;


import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuan">guan xiangHuan</a>
 * @version 0.0.2 2020-04-02
 * @since JDK8.0
 */
@Deprecated
public class LRU<T> extends ReentrantReadWriteLock implements ExpiryPolicy<T> {
    /**
     * Linked list to maintain order that cache objects are accessed in, most
     * used to least used.
     */
    private final LinkedList<T> lru = new LinkedList<T>();

    @Override
    public void clear() {
        writeLock().lock();
        try {
            lru.clear();
        } finally {
            writeLock().unlock();
        }
    }

    @Override
    public void offerFirst(T t) {
        writeLock().lock();
        try {
            lru.remove(t);
            lru.offerFirst(t);
        } finally {
            writeLock().unlock();
        }
    }

    @Override
    public T peekLast() {
        readLock().lock();
        try {
            return lru.peekLast();
        } finally {
            readLock().unlock();
        }
    }

    @Override
    public boolean remove(T t) {
        writeLock().lock();
        try {
            return lru.remove(t);
        } finally {
            writeLock().unlock();
        }
    }
}
