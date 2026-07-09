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

package salt.hoprxi.cache;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import salt.hoprxi.cache.caffeine.CaffeineCache;
import salt.hoprxi.cache.l1_2.L1_2Cache;
import salt.hoprxi.cache.redis.RedisCache;
import salt.hoprxi.cache.redis.RedisClient;
import salt.hoprxi.cache.redis.lettuce.LettuceClusterRedisClient;
import salt.hoprxi.cache.redis.lettuce.LettuceStandAloneRedisClient;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.3 2022-07-17
 */
public class CacheFactory {
    /*
    private static final int UNSET_INT = -1;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final int DEFAULT_EXPIRATION_NANOS = 0;
    private long maximumSize = UNSET_INT;
    private long maximumWeight = UNSET_INT;
    private int initialCapacity = UNSET_INT;
     */
    private static final Map<String, Cache<?, ?>> CACHE_CACHE = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger("salt.hoprxi.cache");
    private static Config config;

    static {
        Config base;
        try {
            Config cache = ConfigFactory.load("cache");
            Config units = ConfigFactory.load("cache_unit");
            base = cache.withFallback(units).resolve();
        } catch (Exception e) {
            LoggerFactory.getLogger(CacheFactory.class)
                    .warn("Config load failed, using built-in default. Error: {}", e.getMessage());
            base = ConfigFactory.empty();
        }
        String defaultConfigStr = "public_example.provider = caffeine\n";
        Config defaultConfig = ConfigFactory.parseString(defaultConfigStr);
        config = base.withFallback(defaultConfig);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Cache<K, V> build(String region) {
        region = Objects.requireNonNull(region, "region is required").trim();
        String finalRegion = region;
        return (Cache<K, V>) CACHE_CACHE.computeIfAbsent(region, v -> {
            if (config.hasPath(finalRegion + ".provider")) {
                Config userConfig = config.getConfig(finalRegion);
                return create(userConfig, finalRegion);
            } else {
                return create(config.getConfig("public_example"), finalRegion);
            }
        });
    }

    private static <K, V> Cache<K, V> create(Config userConfig, String region) {
        Cache<K, V> cache;
        String provider = userConfig.getString("provider");
        switch (provider) {
            case "caffeine":
                cache = new CaffeineCache<>(userConfig.getConfig("caffeine"));
                break;
            case "redis":
                Config redisConfig = userConfig.getConfig("redis");
                String redisClient = redisConfig.getString("redisClient");
                cache = getRedisCache(region, redisConfig, redisClient);
                break;
            case "l1_2":
                Config localConfig = userConfig.getConfig("l1");
                String localProvider = localConfig.getString("provider");
                Cache<K, V> localCache = null;
                if ("caffeine".equals(localProvider)) {
                    localCache = new CaffeineCache<>(localConfig.getConfig("caffeine"));
                }
                Config remoteConfig = userConfig.getConfig("l2");
                String remoteProvider = remoteConfig.getString("provider");
                Cache<K, V> remoteCache = null;
                if ("redis".equals(remoteProvider)) {
                    Config l2RedisConfig = remoteConfig.getConfig("redis");
                    String l2RedisClient = l2RedisConfig.getString("redisClient");
                    remoteCache = getRedisCache(region, l2RedisConfig, l2RedisClient);
                }
                cache = new L1_2Cache<>(localCache, remoteCache);
                break;
            default:
                // 未知 provider 时，默认创建 Caffeine 缓存
                LOGGER.warn("Unknown provider '{}', fallback to caffeine", provider);
                cache = new CaffeineCache<>(ConfigFactory.empty());
                break;
        }
        return cache;
    }

    private static <K, V> Cache<K, V> getRedisCache(String region, Config redisConfig, String redisClient) {
        Cache<K, V> cache = null;
        switch (redisClient) {
            case "standAlone":
                RedisClient<K, V> standAlone = new LettuceStandAloneRedisClient<>(region, redisConfig.getConfig("standAlone"));
                cache = new RedisCache<>(region, standAlone);
                break;
            case "sentinel":
                break;
            case "cluster":
                RedisClient<K, V> cluster = new LettuceClusterRedisClient<>(region, redisConfig.getConfig("cluster"));
                cache = new RedisCache<>(region, cluster);
                break;
        }
        return cache;
    }

    /**
     * 使用扩展配置构建缓存，但不影响全局 CONFIG。
     * 注意：如果该 region 已经存在于缓存池中，此方法不会重建缓存，而是返回已有实例。
     * 若希望强制重建，请先调用 evictRegion(region) 清除缓存。
     */
    public static <K, V> Cache<K, V> build(String region, Config extend) {
        config = config.withFallback(extend);
        return CacheFactory.build(region);
    }
}
