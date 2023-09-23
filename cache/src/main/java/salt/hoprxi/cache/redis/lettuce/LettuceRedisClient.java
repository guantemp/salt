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

package salt.hoprxi.cache.redis.lettuce;

import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.pool2.impl.GenericObjectPool;
import salt.hoprxi.cache.redis.RedisClient;
import salt.hoprxi.cache.util.Serialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2022-07-24
 */
public abstract class LettuceRedisClient<K, V> implements RedisClient<K, V> {
    protected static final LettuceByteRedisCodec LETTUCE_BYTE_REDIS_CODEC = new LettuceByteRedisCodec();
    protected static final byte[] SEPARATOR = ":".getBytes(StandardCharsets.UTF_8);
    protected static final long SCAN_COUNT = 10000;
    protected final byte[] regionBytes;
    protected GenericObjectPool<StatefulConnection<byte[], byte[]>> pool;
    protected long expire;
    protected Serialization serialization;

    /**
     * @param region
     * @throws IllegalArgumentException No configuration setting found
     */
    public LettuceRedisClient(String region) {
        regionBytes = region.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public abstract void set(K key, V value);

    @Override
    public abstract void hset(K key, V value);

    @Override
    public abstract void set(Map<? extends K, ? extends V> map);

    @Override
    public abstract V get(K key);

    @Override
    public abstract V hget(K key);

    @Override
    public abstract Map<K, V> get(K... keys);

    @Override
    public abstract Map<K, V> get(Iterable<? extends K> keys);

    @Override
    public abstract void hdel(K key);

    @Override
    public abstract void del(K key);

    @Override
    public abstract void clear();

    @Override
    public abstract void close() throws IOException;

    protected byte[] merge(K key) {
        byte[] keyBytes = serialization.serialize(key);
        byte[] result = new byte[regionBytes.length + SEPARATOR.length + keyBytes.length];
        System.arraycopy(regionBytes, 0, result, 0, regionBytes.length);
        System.arraycopy(SEPARATOR, 0, result, regionBytes.length, SEPARATOR.length);
        System.arraycopy(keyBytes, 0, result, regionBytes.length + SEPARATOR.length, keyBytes.length);
        return result;
    }
}
