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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
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
 * @version 0.0.1 2019-04-15
 */
public class SentinelJedisClient<K, V> implements JedisClient<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SentinelJedisClient.class);
    private static final String CHARSET = StandardCharsets.UTF_8.name();
    private JedisSentinelPool pool;
    private Serialization serialization;

    public SentinelJedisClient(Config config) {
        this(config, ConfigFactory.parseURL(UrlHelper.toUrlWithPoint("resources.cache_reference.conf")));
    }

    /**
     * @param config
     * @param reference
     */
    public SentinelJedisClient(Config config, Config reference) {
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
    public V put(K key, String field, V value) {
        try (Jedis jedis = pool.getResource()) {
            byte[] fieldBytes = field.getBytes(CHARSET);
            byte[] keyBytes = serialization.serialize(key);
            byte[] bytes = jedis.hget(keyBytes, fieldBytes);
            jedis.hset(keyBytes, fieldBytes, serialization.serialize(value));
            if (bytes != null)
                return serialization.deserialize(bytes);
        } catch (UnsupportedEncodingException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Unsupported Encoding {}", CHARSET);
        }
        return null;
    }

    @Override
    public V get(K key, String field) {
        try (Jedis jedis = pool.getResource()) {
            byte[] bytes = jedis.hget(serialization.serialize(key), field.getBytes(CHARSET));
            if (bytes != null)
                return serialization.deserialize(bytes);
        } catch (UnsupportedEncodingException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Unsupported Encoding {}", CHARSET);
        }
        return null;
    }

    @Override
    public V remove(K key, String field) {
        try (Jedis jedis = pool.getResource()) {
            byte[] bytes = jedis.hget(serialization.serialize(key), field.getBytes(CHARSET));
            jedis.hdel(serialization.serialize(key), field.getBytes(CHARSET));
            if (bytes != null)
                return serialization.deserialize(bytes);
        } catch (UnsupportedEncodingException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Unsupported Encoding {}", CHARSET);
        }
        return null;
    }

    @Override
    public void clear(String field) {
        Jedis jedis = pool.getResource();
        jedis.close();
    }
}
