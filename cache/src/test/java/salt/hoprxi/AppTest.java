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

package salt.hoprxi;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.testng.annotations.Test;
import salt.hoprxi.cache.Cache;
import salt.hoprxi.cache.redis.RedisCacheBuilder;
import salt.hoprxi.cache.redis.RedisClient;
import salt.hoprxi.cache.redis.lettuce.LettuceStandAloneRedisClient;

import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest {
    @Test
    public void cacheTest() {
        final Config config = ConfigFactory.load("cache").getConfig("redis_standAlone");
        final RedisClient<Integer, Object> client = new LettuceStandAloneRedisClient("test", config);
        final Cache<Integer, Object> cache = new RedisCacheBuilder<Integer, Object>("test").client(client).build();
        cache.put(9, "发的啥大事阿尔山和我");
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        Config config = ConfigFactory.load("cache");
        Config reference = ConfigFactory.load("cache_unit");
        config = config.withFallback(reference);
        System.out.println("Seconds:" + config.getDuration("public_example.l2.redis.standAlone.expire").getSeconds());
        List<String> list = config.getStringList("redis_cluster.hosts");
        for (String s : list) {
            String[] redisArray = s.split(":");
            System.out.print(redisArray[0]);
            System.out.print(":");
            System.out.println(redisArray[1]);
        }
        System.out.println(config.getString("item_view.l1.caffeine.expire"));
        System.out.println(config.getString("public_example.provider"));
        Config l1 = config.getConfig("public_example.l1");
        System.out.println(l1.getString("caffeine.expire"));
        Config l2 = config.getConfig("item_view.l2");
        System.out.println(l2.getString("redis.standAlone.serialization"));
    }
}
