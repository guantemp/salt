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

package salt.hoprxi.cache.l2.redis.lettuce;

import com.typesafe.config.Config;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import salt.hoprxi.cache.l2.redis.jedis.JedisClient;
import salt.hoprxi.cache.l2.redis.serialization.FSTSerialization;
import salt.hoprxi.cache.l2.redis.serialization.KryoSerialization;
import salt.hoprxi.cache.l2.redis.serialization.Serialization;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.1 2020-09-11
 * @since JDK8.0
 */
public class LettuceReStandAloneRedisClient<K, V> implements JedisClient<K, V> {
    private RedisClient client;
    private RedisCommands<K, V> command;
    private Serialization serialization;

    public LettuceReStandAloneRedisClient(Config config) {
        RedisURI uri = RedisURI.builder()
                .withHost(config.getString("redis.host"))
                .withPort(config.getInt("redis.port"))
                .withTimeout(Duration.of(config.getInt("redis.timeout"), ChronoUnit.MILLIS))
                .build();
        serialization = config.getString("redis.serialization").equalsIgnoreCase("KryoSerialization") ? new KryoSerialization() : new FSTSerialization();
        client = RedisClient.create(uri);
    }

    @Override
    public V put(K key, String field, V value) {
        StatefulRedisConnection<K, V> connection = client.connect(new CacheRedisCodec<K, V>(serialization));
        command = connection.sync();
        V oldValue = command.get(key);

        //command.hset(key,field,value);
        connection.close();
        client.shutdown();
        return oldValue;
    }

    @Override
    public V get(K key, String field) {
        return null;
    }

    @Override
    public V remove(K key, String filed) {
        return null;
    }

    @Override
    public void clear(String field) {

    }
}
