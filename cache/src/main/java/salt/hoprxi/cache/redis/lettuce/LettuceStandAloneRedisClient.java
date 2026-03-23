/*
 * Copyright (c) 2025. www.hoprxi.com All Rights Reserved.
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

import com.typesafe.config.Config;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import salt.hoprxi.cache.util.KryoSerialization;
import salt.hoprxi.crypto.application.DatabaseSpecDecrypt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.1.1 2025-07-27
 * @since JDK21
 */
public class LettuceStandAloneRedisClient<K, V> extends LettuceRedisClient<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LettuceStandAloneRedisClient.class);
    private final RedisClient client;
    private final StatefulRedisConnection<byte[], byte[]> connection;

    public LettuceStandAloneRedisClient(String region, Config config) {
        super(region);
        String host = config.getString("host");
        int port = config.getInt("port");
        //System.out.println(host + ":" + port);
        RedisURI uri = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withPassword(DatabaseSpecDecrypt.decrypt(host + ":" + port, config.getString("password")).toCharArray())
                .withTimeout(config.hasPath("timeout") ? config.getDuration("timeout") : Duration.ofSeconds(2))
                .withDatabase(config.hasPath("database") ? config.getInt("database") : 0)
                .build();
        client = RedisClient.create(uri);
        connection = client.connect(LETTUCE_BYTE_REDIS_CODEC); // 保存为成员变量
        expire = config.hasPath("expire") ? config.getLong("expire") : 0L;
        /*
        serialization = "kryo".equalsIgnoreCase(
                config.hasPath("serialization") ? config.getString("serialization") : "kryo"
        ) ? new KryoSerialization() : new FSTSerialization();
         */
        serialization = new KryoSerialization();
    }

    @Override
    public void set(K key, V value) {
        try {
            RedisCommands<byte[], byte[]> sync = connection.sync(); // 同步命令接口
            byte[] _key = merge(key);
            //System.out.println("SET key hex: {}"+ Base64.getEncoder().encodeToString(_key));
            byte[] _value = serialization.serialize(value);

            if (expire > 0) {// 原子操作：设置值 + 过期时间（毫秒）
                sync.psetex(_key, expire, _value);
            } else {
                sync.set(_key, _value);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to set key: {}", key, e);
            // 根据业务需求：可选择抛出异常 or 静默失败
            //throw new CacheException("Redis set failed", e);
        }
    }

    @Override
    public V get(K key) {
        try {
            byte[] _key = merge(key);
            //System.out.println("get key hex: {}"+ Base64.getEncoder().encodeToString(_key));
            byte[] result = connection.sync().get(_key);
            if (result == null) {
                return null;
            }
            try {
                return serialization.deserialize(result);
            } catch (Exception de) {
                // 单独记录反序列化错误（便于排查）
                LOGGER.error("Deserialization failed for key: {}, data length: {}", key, result.length, de);
                return null; // 或 throw new CacheCorruptionException("...");
            }
        } catch (RedisException e) {
            // 只捕获真正的 Redis/网络异常
            LOGGER.warn("Redis connection error for key: {}", key, e);
            return null;
        }
    }

    @Override
    public void hset(K key, V value) {
        try {
            RedisCommands<byte[], byte[]> sync = connection.sync();
            byte[] _key = merge(key);
            byte[] _value = serialization.serialize(value);
            sync.hset(regionBytes, _key, _value); // 1. 写入 hash field
            if (expire > 0) {// 2. 设置整个 hash key 的过期时间（Redis 不支持 field 级 TTL）
                sync.pexpire(regionBytes, expire); // 毫秒级过期
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to hset key: {} in hash: {}", key, regionBytes, e);
            // 可选：根据业务决定是否抛出异常
            // throw new CacheException("Redis hset failed", e);
        }
    }

    @Override
    public V hget(K key) {
        try {
            RedisCommands<byte[], byte[]> sync = connection.sync();
            byte[] fieldKey = merge(key); // ← 必须和 hset 完全一致！
            byte[] result = sync.hget(regionBytes, fieldKey);
            return result == null ? null : serialization.deserialize(result);
        } catch (Exception e) {
            LOGGER.warn("Failed to hget key: {} from hash: {}", key, regionBytes, e);
            return null;
        }
    }

    @Override
    public void set(Map<? extends K, ? extends V> map) {
        if (map == null || map.isEmpty()) return;
        try {
            RedisCommands<byte[], byte[]> sync = connection.sync();
            for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                byte[] _key = merge(entry.getKey());
                byte[] _value = serialization.serialize(entry.getValue());
                if (expire > 0) {
                    sync.psetex(_key, expire, _value); // 原子操作
                } else {
                    sync.set(_key, _value);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to set batch keys: {}", map.keySet(), e);
        }
    }

    @Override
    public Map<K, V> get(Iterable<? extends K> keys) {
        if (keys == null) {
            return Collections.emptyMap();
        }

        List<K> keyList = new ArrayList<>();
        List<byte[]> rawKeys = new ArrayList<>();
        for (K key : keys) {
            if (key != null) {
                keyList.add(key);
                rawKeys.add(merge(key));
            }
        }

        RedisCommands<byte[], byte[]> sync = connection.sync();
        List<KeyValue<byte[], byte[]>> values = sync.mget(rawKeys.toArray(byte[][]::new));
        Map<K, V> result = new HashMap<>((int) (keyList.size() / 0.75f + 1));

        for (int i = 0, j = values.size(); i < j; i++) {// ← 这是原始 key（和你传入的一致）
            if (values.get(i).hasValue()) {
                try {
                    V value = serialization.deserialize(values.get(i).getValue());
                    result.put(keyList.get(i), value);
                } catch (Exception e) {
                    LOGGER.warn("Deserialization failed for key: {}", keyList.get(i), e);
                    // 跳过，不 put
                }
            }
        }
        return Collections.unmodifiableMap(result);
    }

    @SafeVarargs
    @Override
    public final Map<K, V> get(K... keys) {
        return this.get(Arrays.asList(keys));
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> function) {
        V result = this.get(key);
        if (result == null) {
            result = function.apply(key);
            if (result != null)
                this.set(key, result);
        }
        return result;
    }

    @Override
    public Map<K, V> get(Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
        Map<K, V> normal = this.get(keys);
        /*
        int capacity = (int) (((Collection<?>) keys).size() / 0.75 + 1);
        Set<K> undiscovered = new HashSet<>(capacity);
        for (K k : keys) {
            if (!normal.containsKey(k))
                undiscovered.append(k);
        }
         */
        Set<K> undiscovered = StreamSupport.stream(keys.spliterator(), true).filter(k -> !normal.containsKey(k)).collect(Collectors.toSet());
        Map<K, V> additional = (Map<K, V>) mappingFunction.apply(undiscovered);
        Map<K, V> result = Stream.of(normal, additional)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v2));
        return Collections.unmodifiableMap(result);
    }

    @Override
    public void hdel(K key) {
        if (key == null) {
            return;
        }
        try {
            RedisCommands<byte[], byte[]> sync = connection.sync();
            byte[] fieldKey = merge(key); // ← 必须和 hset 完全一致！
            sync.hdel(regionBytes, fieldKey);
        } catch (Exception e) {
            LOGGER.warn("Failed to delete hash field: {} from hash key: {}", key, regionBytes, e);
        }
    }

    @Override
    public void del(K key) {
        if (key == null) {
            return;
        }
        try {
            RedisCommands<byte[], byte[]> sync = connection.sync();
            byte[] _key = merge(key); // 确保和 set() 一致
            sync.del(_key);
        } catch (Exception e) {
            LOGGER.warn("Failed to delete key: {}", key, e);
        }
    }

    @SafeVarargs
    @Override
    public final void del(K... keys) {
        this.del(Arrays.asList(keys));
    }

    @Override
    public void del(Iterable<? extends K> keys) {
        if (keys == null) {
            return;
        }

        // 安全收集非 null key 的 raw bytes
        List<byte[]> rawKeys = new ArrayList<>();
        for (K key : keys) {
            if (key != null) {
                rawKeys.add(merge(key));
            }
        }

        if (rawKeys.isEmpty()) {
            return;
        }

        try {
            RedisCommands<byte[], byte[]> sync = connection.sync(); // 单例连接
            sync.del(rawKeys.toArray(byte[][]::new)); // 或 new byte[rawKeys.size()][]
        } catch (Exception e) {
            LOGGER.warn("Failed to delete keys: {}", rawKeys, e);
        }
    }

    @Override
    public void clear() {
        try {
            RedisCommands<byte[], byte[]> sync = connection.sync();

            Collection<byte[]> allKeys = new ArrayList<>();
            ScanCursor cursor = ScanCursor.INITIAL;
            ScanArgs args = ScanArgs.Builder.matches(new String(regionBytes, StandardCharsets.UTF_8) + ":" + "*")
                    .limit(SCAN_COUNT);

            // 收集所有匹配 key
            while (!cursor.isFinished()) {
                KeyScanCursor<byte[]> scanResult = sync.scan(cursor, args);
                allKeys.addAll(scanResult.getKeys());
                cursor = scanResult;
            }

            // 分批异步删除（避免阻塞 Redis）
            List<byte[]> keyList = new ArrayList<>(allKeys);
            int batchSize = 100; // 避免大 DEL 阻塞
            for (int i = 0; i < keyList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, keyList.size());
                byte[][] batch = keyList.subList(i, end).toArray(byte[][]::new);
                // 使用 UNLINK（异步删除，非阻塞）
                sync.unlink(batch);
            }

        } catch (Exception e) {
            LOGGER.warn("Failed to clear cache region: {}",
                    new String(regionBytes, StandardCharsets.UTF_8), e);
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.close(); // 1. 关闭连接
            }
        } catch (Exception e) {
            LOGGER.warn("Error closing Redis connection", e);
        } finally {
            if (client != null) {
                client.shutdown(); // 2. 关闭客户端（释放 Netty 资源）
            }
        }
    }
}
