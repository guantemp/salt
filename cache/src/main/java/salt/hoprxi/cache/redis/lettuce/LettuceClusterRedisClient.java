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

import com.typesafe.config.Config;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import salt.hoprxi.cache.util.FSTSerialization;
import salt.hoprxi.cache.util.KryoSerialization;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2022-07-19
 */
public class LettuceClusterRedisClient<K, V> extends LettuceRedisClient<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LettuceStandAloneRedisClient.class);
    //定时5分钟更新集群拓扑视图
    private static final Duration PERIODIC_REFRESH = Duration.ofMinutes(5);
    private final RedisClusterClient client;

    public LettuceClusterRedisClient(String region, Config config) {
        super(region);
        List<RedisURI> redisURIS = new ArrayList<>();
        List<String> hosts = config.getStringList("hosts");
        for (String host : hosts) {
            String[] temp = host.split(":");
            RedisURI.Builder builder = RedisURI.builder();
            builder.withHost(temp[0])
                    .withPort(Integer.parseInt(temp[1]))
                    .withTimeout(config.hasPath("timeout") ? config.getDuration("timeout") : Duration.ZERO)
                    .withDatabase(config.hasPath("database") ? config.getInt("database") : 0);
            if (config.hasPath("password"))
                builder.withPassword(config.getString("password").toCharArray());
            RedisURI uri = builder.build();
            redisURIS.add(uri);
        }
        client = RedisClusterClient.create(redisURIS);

        ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                //自适应更新集群拓扑视图，超时5秒
                .enableAdaptiveRefreshTrigger(ClusterTopologyRefreshOptions.RefreshTrigger.MOVED_REDIRECT, ClusterTopologyRefreshOptions.RefreshTrigger.PERSISTENT_RECONNECTS)
                .enableAllAdaptiveRefreshTriggers()
                .adaptiveRefreshTriggersTimeout(Duration.ofMillis(5000))
                //定时更新集群拓扑视图
                .enablePeriodicRefresh(PERIODIC_REFRESH)
                .build();
        client.setOptions(ClusterClientOptions.builder().topologyRefreshOptions(topologyRefreshOptions).build());

        GenericObjectPoolConfig<StatefulConnection<byte[], byte[]>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(config.hasPath("maxTotal") ? config.getInt("maxTotal") : 8);
        poolConfig.setMaxIdle(config.hasPath("maxIdle") ? config.getInt("maxIdle") : 8);
        poolConfig.setMinIdle(config.hasPath("minIdle") ? config.getInt("minIdle") : 1);
        pool = ConnectionPoolSupport.createGenericObjectPool(() -> client.connect(LETTUCE_BYTE_REDIS_CODEC), poolConfig);

        expire = config.hasPath("expire") ? config.getLong("expire") : 0L;
        serialization = config.hasPath("serialization") ?
                config.getString("serialization").equalsIgnoreCase("kryo") ?
                        new KryoSerialization() : new FSTSerialization() : new KryoSerialization();
    }

    @Override
    public void set(K key, V value) {
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = (StatefulRedisClusterConnection<byte[], byte[]>) pool.borrowObject()) {
            RedisAdvancedClusterCommands<byte[], byte[]> command = connection.sync();
            byte[] _key = merge(key);
            byte[] valueBytes = serialization.serialize(value);
            command.set(_key, valueBytes);
            command.pexpire(_key, expire);
        } catch (Exception e) {
            LOGGER.warn("Can't put {key={},value={}} in cache", key, value, e);
        }
    }

    @Override
    public void hset(K key, V value) {
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = (StatefulRedisClusterConnection<byte[], byte[]>) pool.borrowObject()) {
            RedisAdvancedClusterCommands<byte[], byte[]> command = connection.sync();
            byte[] keyBytes = serialization.serialize(key);
            byte[] valueBytes = serialization.serialize(value);
            command.hset(regionBytes, keyBytes, valueBytes);
        } catch (Exception e) {
            LOGGER.warn("Can't put {key={}} in cache", value, e);
        }
    }

    @Override
    public void set(Map<? extends K, ? extends V> map) {
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = (StatefulRedisClusterConnection<byte[], byte[]>) pool.borrowObject()) {
            RedisAdvancedClusterCommands<byte[], byte[]> command = connection.sync();
            map.forEach((key, value) -> {
                byte[] _key = merge(key);
                byte[] valueBytes = serialization.serialize(value);
                command.set(_key, valueBytes);
                command.pexpire(_key, expire);
            });
        } catch (Exception e) {
            LOGGER.warn("Can't put {} in cache", map, e);
        }
    }

    @Override
    public V get(K key) {
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = (StatefulRedisClusterConnection<byte[], byte[]>) pool.borrowObject()) {
            RedisAdvancedClusterAsyncCommands<byte[], byte[]> command = connection.async();
            byte[] _key = merge(key);
            RedisFuture<byte[]> redisFuture = command.get(_key);
            byte[] result = redisFuture.get();

            return serialization.deserialize(result);
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.warn("Can't rebuild value from key={}", key, e);
        } catch (Exception e) {
            //System.out.println(e);
            LOGGER.warn("Can't value value from key={}", key, e);
        }
        return null;
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> function) {
        V result = get(key);
        if (result == null) {
            result = function.apply(key);
            if (result != null)
                set(key, result);
        }
        return result;
    }

    @Override
    public V hget(K key) {
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = (StatefulRedisClusterConnection<byte[], byte[]>) pool.borrowObject()) {
            RedisAdvancedClusterAsyncCommands<byte[], byte[]> command = connection.async();
            byte[] keyBytes = serialization.serialize(key);
            RedisFuture<byte[]> redisFuture = command.hget(regionBytes, keyBytes);
            return serialization.deserialize(redisFuture.get());
        } catch (Exception e) {
            LOGGER.debug("Can't put cache", e);
        }
        return null;
    }


    @SafeVarargs
    @Override
    public final Map<K, V> get(K... keys) {
        return get(Arrays.asList(keys));
    }

    @Override
    public Map<K, V> get(Iterable<? extends K> keys) {
        int capacity = (int) (((Collection<?>) keys).size() / 0.75 + 1);
        Map<K, V> result = new HashMap<>(capacity);
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = (StatefulRedisClusterConnection<byte[], byte[]>) pool.borrowObject()) {

            RedisAdvancedClusterAsyncCommands<byte[], byte[]> command = connection.async();
            command.setAutoFlushCommands(false);
            List<RedisFuture<byte[]>> redisFutureList = new ArrayList<>();
            for (K key : keys) {
                byte[] _key = merge(key);
                redisFutureList.add(command.get(_key));
            }
            command.flushCommands();
            command.setAutoFlushCommands(true);
            int i = 0;
            for (K key : keys) {
                V v = serialization.deserialize(redisFutureList.get(i++).get());
                if (v != null)
                    result.put(key, v);
            }
        } catch (Exception e) {
            LOGGER.warn("Can't value value from keys={}", keys, e);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<K, V> get(Iterable<? extends K> keys, Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
        Map<K, V> normal = get(keys);
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
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = (StatefulRedisClusterConnection<byte[], byte[]>) pool.borrowObject()) {
            RedisAdvancedClusterCommands<byte[], byte[]> command = connection.sync();
            byte[] keyBytes = serialization.serialize(key);
            command.hdel(regionBytes, keyBytes);
        } catch (Exception e) {
            LOGGER.warn("Can't del {} from cache", key, e);
        }
    }

    @Override
    public void del(K key) {
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = (StatefulRedisClusterConnection<byte[], byte[]>) pool.borrowObject()) {
            RedisAdvancedClusterCommands<byte[], byte[]> command = connection.sync();
            byte[] _key = merge(key);
            command.del(_key);
        } catch (Exception e) {
            LOGGER.warn("Can't del key={} from cache", key, e);
        }
    }

    @SafeVarargs
    @Override
    public final void del(K... keys) {
        del(Arrays.asList(keys));
    }

    @Override
    public void del(Iterable<? extends K> keys) {
        int size = ((Collection<?>) keys).size();
        byte[][] bytes = new byte[size][];
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = (StatefulRedisClusterConnection<byte[], byte[]>) pool.borrowObject()) {
            RedisAdvancedClusterCommands<byte[], byte[]> command = connection.sync();
            int i = 0;
            for (K key : keys) {
                bytes[i++] = merge(key);
            }
            command.del(bytes);
        } catch (Exception e) {
            LOGGER.warn("Can't clear cache", e);
        }
    }

    @Override
    public void clear() {
        try (StatefulRedisClusterConnection<byte[], byte[]> connection = (StatefulRedisClusterConnection<byte[], byte[]>) pool.borrowObject()) {
            RedisAdvancedClusterCommands<byte[], byte[]> command = connection.sync();
            Collection<byte[]> keys = new ArrayList<>();
            ScanCursor scanCursor = ScanCursor.INITIAL;
            ScanArgs scanArgs = new ScanArgs();
            scanArgs.match(new String(regionBytes) + new String(SEPARATOR) + "*").limit(SCAN_COUNT);
            KeyScanCursor<byte[]> keyScanCursor;
            while (!scanCursor.isFinished()) {
                keyScanCursor = command.scan(scanCursor, scanArgs);
                keys.addAll(keyScanCursor.getKeys());
                scanCursor = keyScanCursor;
            }
            int size = keys.size();
            byte[][] bytes = new byte[size][];
            int i = 0;
            for (byte[] b : keys) {
                bytes[i++] = b;
            }
            command.del(bytes);
        } catch (Exception e) {
            LOGGER.warn("Can't clear cache", e);
        }
    }

    @Override
    public void close() {
        client.shutdown();
    }
}
