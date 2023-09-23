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
package salt.hoprxi.cache.redis.jedis;

import com.typesafe.config.Config;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import salt.hoprxi.cache.redis.RedisClient;
import salt.hoprxi.cache.util.FSTSerialization;
import salt.hoprxi.cache.util.KryoSerialization;
import salt.hoprxi.cache.util.Serialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;


/**
 * Allow single machine multiple instances but port not same
 *
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.2 2021-08-11
 * @since JDK8.0
 */
@Deprecated
public class JedisStandAloneRedisClient<K, V> implements RedisClient<K, V> {
    private static final String CHARSET = StandardCharsets.UTF_8.name();
    private final JedisPool pool;
    private final Serialization serialization;

    public JedisStandAloneRedisClient(Config config) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(config.getInt("maxTotal"));
        jedisPoolConfig.setMaxIdle(config.getInt("maxIdle"));
        jedisPoolConfig.setMinIdle(config.getInt("minIdle"));
        jedisPoolConfig.setMaxWaitMillis(config.getInt("maxWaitMillis"));
        //jedisPoolConfig.setTestOnBorrow(config.getBoolean("testOnBorrow"));
        //jedisPoolConfig.setTestOnReturn(config.getBoolean("testOnReturn"));
        //jedisPoolConfig.setTestWhileIdle(config.getBoolean("testWhileIdle"));
        jedisPoolConfig.setTestOnBorrow(false);
        String host = config.getString("host");
        int port = config.getInt("port");
        pool = config.hasPath("password") ? new JedisPool(jedisPoolConfig, host, port, config.getInt("timeout"), config.getString("password")) :
                new JedisPool(jedisPoolConfig, host, port);
        serialization = config.getString("serialization").equalsIgnoreCase("KryoSerialization") ? new KryoSerialization() : new FSTSerialization();
    }


    @Override
    public void set(K key, V value) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(serialization.serialize(key), serialization.serialize(value));
        }
    }

    @Override
    public void hset(K key, V value) {
        try (Jedis jedis = pool.getResource()) {
            byte[] keyBytes = serialization.serialize(key);
            byte[] valueBytes = serialization.serialize(value);
            jedis.hset(keyBytes, keyBytes, valueBytes);
        }
    }

    @Override
    public void set(Map<? extends K, ? extends V> map) {

    }


    @Override
    public V get(K key) {
        try (Jedis jedis = pool.getResource()) {
            byte[] bytes = jedis.get(serialization.serialize(key));
            if (bytes != null)
                return serialization.deserialize(bytes);
            return null;
        }
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> function) {
        return null;
    }

    @Override
    public Map<K, V> get(K... keys) {
        return null;
    }

    @Override
    public Map<K, V> get(Iterable<? extends K> keys) {
        return null;
    }

    @Override
    public Map<K, V> get(Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
        return null;
    }

    @Override
    public V hget(K key) {
        try (Jedis jedis = pool.getResource()) {
           /* KeyWrap<K> tempKey = new KeyWrap<>(key, key);
            byte[] bytes = jedis.value(serialization.serialize(tempKey));
            if (bytes != null)
                return serialization.deserialize(bytes);
            return null;

            */
        }
        return null;
    }


    @Override
    public void hdel(K key) {
        /*
        try (Jedis jedis = pool.getResource()) {
            KeyWrap<K> tempKey = new KeyWrap<>(field, key);
            jedis.del(serialization.serialize(tempKey));
            return null;
        }

         */
    }

    @Override
    public void del(K key) {
    }

    @Override
    public void del(K... keys) {

    }

    @Override
    public void del(Iterable<? extends K> keys) {

    }

    @Override
    public void clear() {
        Jedis jedis = pool.getResource();
        jedis.close();
    }

    @Override
    public void close() throws IOException {
        pool.close();
    }
}
