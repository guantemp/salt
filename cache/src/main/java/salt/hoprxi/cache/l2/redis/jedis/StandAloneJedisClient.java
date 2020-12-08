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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import salt.hoprxi.cache.l2.redis.KeyWrap;
import salt.hoprxi.cache.l2.redis.serialization.FSTSerialization;
import salt.hoprxi.cache.l2.redis.serialization.KryoSerialization;
import salt.hoprxi.cache.l2.redis.serialization.Serialization;


/**
 * Allow single machine multiple instances but port not same
 *
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.1 2019-03-15
 * @since JDK8.0
 */
public class StandAloneJedisClient<K, V> implements JedisClient<K, V> {
    private JedisPool pool;
    private Serialization serialization;

    public StandAloneJedisClient(Config config) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(config.getInt("redis.maxTotal"));
        jedisPoolConfig.setMaxIdle(config.getInt("redis.maxIdle"));
        jedisPoolConfig.setMinIdle(config.getInt("redis.minIdle"));
        jedisPoolConfig.setMaxWaitMillis(config.getInt("redis.maxWaitMillis"));
        jedisPoolConfig.setTestOnBorrow(config.getBoolean("redis.testOnBorrow"));
        jedisPoolConfig.setTestOnReturn(config.getBoolean("redis.testOnReturn"));
        jedisPoolConfig.setTestWhileIdle(config.getBoolean("redis.testWhileIdle"));
        String host = config.getString("redis.host");
        int port = config.getInt("redis.port");
        pool = config.hasPath("redis.password") ? new JedisPool(jedisPoolConfig, host, port, config.getInt("redis.timeout"), config.getString("redis.password")) :
                new JedisPool(jedisPoolConfig, host, port);
        serialization = config.getString("redis.serialization").equalsIgnoreCase("KryoSerialization") ? new KryoSerialization() : new FSTSerialization();
    }


    @Override
    public V put(K key, String field, V value) {
        Jedis jedis = pool.getResource();
        KeyWrap<K> tempKey = new KeyWrap<>(field, key);
        byte[] keyBytes = serialization.serialize(tempKey);
        byte[] bytes = jedis.get(keyBytes);
        jedis.set(keyBytes, serialization.serialize(value));
        if (bytes != null)
            return serialization.deserialize(bytes);
        return null;
    }

    @Override
    public V get(K key, String field) {
        Jedis jedis = pool.getResource();
        KeyWrap<K> tempKey = new KeyWrap<>(field, key);
        byte[] bytes = jedis.get(serialization.serialize(tempKey));
        if (bytes != null)
            return serialization.deserialize(bytes);
        return null;
    }

    @Override
    public V remove(K key, String field) {
        Jedis jedis = pool.getResource();
        KeyWrap<K> tempKey = new KeyWrap<>(field, key);
        jedis.del(serialization.serialize(tempKey));

        return null;
    }

    @Override
    public void clear(String field) {
        Jedis jedis = pool.getResource();
        jedis.close();
    }

    public void shutdown() {
        if (pool != null)
            pool.close();
    }
}
