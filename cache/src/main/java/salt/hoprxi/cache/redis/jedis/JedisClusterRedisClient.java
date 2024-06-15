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
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import salt.hoprxi.cache.redis.RedisClient;
import salt.hoprxi.cache.util.FSTSerialization;
import salt.hoprxi.cache.util.KryoSerialization;
import salt.hoprxi.cache.util.Serialization;
import salt.hoprxi.utils.ResourceWhere;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.1 2019-03-19
 */
@Deprecated
public class JedisClusterRedisClient<K, V> implements RedisClient<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JedisClusterRedisClient.class);
    private static final String CHARSET = StandardCharsets.UTF_8.name();
    private final JedisCluster jedisCluster;
    private final Serialization serialization;

    public JedisClusterRedisClient(Config config) {
        this(config, ConfigFactory.parseURL(ResourceWhere.toUrlWithPoint("resources.cache_unit.json")));
    }

    /**
     * @param config
     * @param reference default
     */
    public JedisClusterRedisClient(Config config, Config reference) {
        config = config.withFallback(reference);
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(config.getInt("cluster.maxTotal"));
        jedisPoolConfig.setMaxIdle(config.getInt("cluster.maxIdle"));
        jedisPoolConfig.setMinIdle(config.getInt("cluster.minIdle"));
        jedisPoolConfig.setMaxWaitMillis(config.getInt("cluster.maxWaitMillis"));
        jedisPoolConfig.setTestOnBorrow(config.getBoolean("cluster.testOnBorrow"));
        jedisPoolConfig.setTestOnReturn(config.getBoolean("cluster.testOnReturn"));
        jedisPoolConfig.setTestWhileIdle(config.getBoolean("cluster.testWhileIdle"));
        Set<HostAndPort> node = new HashSet<>();
        for (String host : config.getStringList("cluster.hosts"))
            node.add(new HostAndPort(host.split(":")[0], Integer.parseInt(host.split(":")[1])));
        jedisCluster = new JedisCluster(node, jedisPoolConfig);
        serialization = config.getString("redis.serialization").equalsIgnoreCase("KryoSerialization") ? new KryoSerialization() : new FSTSerialization();
    }

    @Override
    public void set(K key, V value) {

    }

    @Override
    public void hset(K key, V value) {
        try {
            byte[] keyBytes = serialization.serialize(key);
            byte[] bytes = serialization.serialize(value);
            jedisCluster.hset(keyBytes, keyBytes, serialization.serialize(value));
        } finally {
            jedisCluster.close();
        }
    }

    @Override
    public void set(Map<? extends K, ? extends V> map) {

    }

    @Override
    public V hget(K key) {
        try {
            byte[] bytes = jedisCluster.hget(serialization.serialize(key), serialization.serialize(key));
            if (bytes != null)
                return serialization.deserialize(bytes);
        } finally {
            jedisCluster.close();
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
        try {
            byte[] bytes = jedisCluster.hget(serialization.serialize(key), field.getBytes(CHARSET));
            jedisCluster.hdel(serialization.serialize(key), field.getBytes(CHARSET));
            if (bytes != null)
                return serialization.deserialize(bytes);
        } catch (UnsupportedEncodingException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Unsupported Encoding {}", CHARSET);
        } finally {
            jedisCluster.close();
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

    }


    @Override
    public void close() throws IOException {
        jedisCluster.close();
    }
}
