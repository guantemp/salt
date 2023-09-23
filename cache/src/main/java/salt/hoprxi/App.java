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

package salt.hoprxi;

import com.typesafe.config.ConfigFactory;
import salt.hoprxi.cache.Cache;
import salt.hoprxi.cache.redis.RedisCacheBuilder;
import salt.hoprxi.cache.redis.RedisClient;
import salt.hoprxi.cache.redis.lettuce.LettuceStandAloneRedisClient;

/**
 * Hello world!
 */
public class App {
    private static final RedisClient<Integer, Object> client = new LettuceStandAloneRedisClient("test", ConfigFactory.load("cache").getConfig("cache.redisCache"));
    private static final Cache<Integer, Object> redisCache = new RedisCacheBuilder<Integer, Object>("test").client(client).build();

    public static void main(String[] args) {
        redisCache.put(1, "2色股份第三个1423");
        redisCache.put(1, "色啊付款金额啊哈");
        redisCache.put(2, "环境恶化和速度就会回来告诉uyyesanmf积分");
        redisCache.put(5, "环的vbddlgkregsdg阿境恶化和速度就会回来告诉uyyesanmf积分");
        String[] s = {"俗话说得好渡水复渡水", "说的话是个射天狼巍峨特务俄问题打了个而社科", "我日我还是发香料"};
        redisCache.put(6, s);
        String[] ss = (String[]) redisCache.get(6);
        for (String s1 : ss)
            System.out.println(s1);
        System.out.println(redisCache.get(5));

    }
}
