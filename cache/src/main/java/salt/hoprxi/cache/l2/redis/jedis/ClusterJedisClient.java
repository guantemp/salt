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

package salt.hoprxi.cache.l2.redis.jedis;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import salt.hoprxi.cache.l2.redis.serialization.FSTSerialization;
import salt.hoprxi.cache.l2.redis.serialization.KryoSerialization;
import salt.hoprxi.cache.l2.redis.serialization.Serialization;
import salt.hoprxi.utils.UrlHelper;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.1 2019-03-19
 */
public class ClusterJedisClient<K, V> implements JedisClient<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterJedisClient.class);
    private static final String CHARSET = StandardCharsets.UTF_8.name();
    private JedisCluster jedisCluster;
    private Serialization serialization;

    public ClusterJedisClient(Config config) {
        this(config, ConfigFactory.parseURL(UrlHelper.toUrlWithPoint("resources.cache_reference.conf")));
    }

    /**
     * @param config
     * @param reference default
     */
    public ClusterJedisClient(Config config, Config reference) {
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
    public V put(K key, String field, V value) {
        try {
            byte[] fieldBytes = field.getBytes(CHARSET);
            byte[] keyBytes = serialization.serialize(key);
            byte[] bytes = jedisCluster.hget(keyBytes, fieldBytes);
            jedisCluster.hset(keyBytes, fieldBytes, serialization.serialize(value));
            if (bytes != null)
                return serialization.deserialize(bytes);
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Unsupported Encoding {}", CHARSET);
        } finally {
            jedisCluster.close();
        }
        return null;
    }

    @Override
    public V get(K key, String field) {
        try {
            byte[] bytes = jedisCluster.hget(serialization.serialize(key), field.getBytes(CHARSET));
            if (bytes != null)
                return serialization.deserialize(bytes);
        } catch (UnsupportedEncodingException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Unsupported Encoding {}", CHARSET);
        } finally {
            jedisCluster.close();
        }
        return null;
    }

    @Override
    public V remove(K key, String field) {
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
        return null;
    }

    @Override
    public void clear(String field) {
        jedisCluster.close();
    }
}
