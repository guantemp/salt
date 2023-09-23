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

package salt.hoprxi.cache.redis.jedis;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import salt.hoprxi.cache.redis.RedisClient;
import salt.hoprxi.cache.util.FSTSerialization;
import salt.hoprxi.cache.util.KryoSerialization;
import salt.hoprxi.cache.util.Serialization;
import salt.hoprxi.utils.ResourceWherePath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.1 2019-04-15
 */
@Deprecated
public class JedisSentinelRedisClient<K, V> implements RedisClient<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JedisSentinelRedisClient.class);
    private static final String CHARSET = StandardCharsets.UTF_8.name();
    private final JedisSentinelPool pool;
    private final Serialization serialization;

    public JedisSentinelRedisClient(Config config) {
        this(config, ConfigFactory.parseURL(ResourceWherePath.toUrlWithPoint("resources.cache_unit.json")));
    }

    /**
     * @param config
     * @param reference
     */
    public JedisSentinelRedisClient(Config config, Config reference) {
        config = config.withFallback(reference);
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(config.getInt("sentinel.maxTotal"));
        jedisPoolConfig.setMaxIdle(config.getInt("sentinel.maxIdle"));
        jedisPoolConfig.setMinIdle(config.getInt("sentinel.minIdle"));
        jedisPoolConfig.setMaxWaitMillis(config.getInt("sentinel.maxWaitMillis"));
        jedisPoolConfig.setTestOnBorrow(config.getBoolean("sentinel.testOnBorrow"));
        jedisPoolConfig.setTestOnReturn(config.getBoolean("sentinel.testOnReturn"));
        jedisPoolConfig.setTestWhileIdle(config.getBoolean("sentinel.testWhileIdle"));
        Set<String> sentinels = new HashSet<>(config.getStringList("sentinel.hosts"));
        pool = config.hasPath("sentinel.password") ? new JedisSentinelPool(config.getString("sentinel.master"), sentinels, jedisPoolConfig, config.getString("sentinel.password")) :
                new JedisSentinelPool(config.getString("sentinel.master"), sentinels, jedisPoolConfig);
        serialization = config.getString("sentinel.serialization").equalsIgnoreCase("KryoSerialization") ? new KryoSerialization() : new FSTSerialization();
    }

    @Override
    public void set(K key, V value) {

    }

    @Override
    public void hset(K key, V value) {
        try (Jedis jedis = pool.getResource()) {
            byte[] keyBytes = serialization.serialize(key);
            byte[] bytes = serialization.serialize(value);
            jedis.hset(keyBytes, keyBytes, serialization.serialize(value));
        }
    }

    @Override
    public void set(Map<? extends K, ? extends V> map) {

    }

    @Override
    public V hget(K key) {
        try (Jedis jedis = pool.getResource()) {
            byte[] bytes = jedis.hget(serialization.serialize(key), serialization.serialize(key));
            if (bytes != null)
                return serialization.deserialize(bytes);
        }
        return null;
    }

    @Override
    public V get(K key) {
        return null;
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
    public void hdel(K key) {
        /*
        try (Jedis jedis = pool.getResource()) {
            byte[] bytes = jedis.hget(serialization.serialize(key), field.getBytes(CHARSET));
            jedis.hdel(serialization.serialize(key), field.getBytes(CHARSET));
            if (bytes != null)
                return serialization.deserialize(bytes);
        } catch (UnsupportedEncodingException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Unsupported Encoding {}", CHARSET);
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
