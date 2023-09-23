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

package salt.hoprxi.cache.redis.lettuce;


import io.lettuce.core.codec.RedisCodec;
import salt.hoprxi.cache.util.Serialization;

import java.nio.ByteBuffer;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.1 2020-09-11
 * @since JDK8.0
 */
public class LettuceRedisCodec<K, V> implements RedisCodec<K, V> {
    private static final byte[] EMPTY = new byte[0];
    private final Serialization serialization;

    protected LettuceRedisCodec(Serialization serialization) {
        this.serialization = serialization;
    }

    private static byte[] redByteBuffer(ByteBuffer buffer) {
        int remaining = buffer.remaining();

        if (remaining == 0) {
            return EMPTY;
        }

        byte[] b = new byte[remaining];
        buffer.get(b);
        return b;
    }

    @Override
    public K decodeKey(ByteBuffer byteBuffer) {
        byte[] bytes = redByteBuffer(byteBuffer);
        if (bytes == EMPTY)
            return null;
        return serialization.deserialize(bytes);
    }

    @Override
    public V decodeValue(ByteBuffer byteBuffer) {
        byte[] bytes = redByteBuffer(byteBuffer);
        if (bytes == EMPTY)
            return null;
        return serialization.deserialize(bytes);
    }

    @Override
    public ByteBuffer encodeKey(Object key) {
        return ByteBuffer.wrap(serialization.serialize(key));
    }

    @Override
    public ByteBuffer encodeValue(Object value) {
        return ByteBuffer.wrap(serialization.serialize(value));
    }
}
