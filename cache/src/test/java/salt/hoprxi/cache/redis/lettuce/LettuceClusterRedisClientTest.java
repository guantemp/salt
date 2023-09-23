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
import com.typesafe.config.ConfigFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import salt.hoprxi.cache.Cache;
import salt.hoprxi.cache.redis.RedisCacheBuilder;
import salt.hoprxi.cache.redis.RedisClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2022-07-29
 */
public class LettuceClusterRedisClientTest {
    private static final Config config = ConfigFactory.load("cache").getConfig("public_example.l2.redis.cluster");
    private static final RedisClient<Integer, Object> client = new LettuceClusterRedisClient("test", config);
    private static final Cache<Integer, Object> cache = new RedisCacheBuilder<Integer, Object>("test").client(client).build();

    @Test
    public void testSet() {
        cache.put(1, "2色股份第三个1423");
        cache.put(2, "环境恶化和速度就会回来告诉uyyesanmf积分");
        cache.put(5, "环的vbddlgkregsdg阿境恶化和速度就会回来告诉uyyesanmf积分");
        String[] s = {"俗话说得好渡水复渡水", "说的话是个射天狼巍峨特务俄问题打了个而社科", "我日我还是发香料"};
        cache.put(6, s);
        Map<Integer, String> map = new HashMap<>();
        map.put(11, "safdas");
        map.put(12, "asd阿凡达2");
        map.put(13, "a杀毒二货·12");
        map.put(14, "沃尔夫大商股份");
        map.put(15, "杀人和渗透疗法");
        cache.put(map);
        cache.put(1, "色啊付款金额啊哈1");
    }

    @Test
    public void testHset() {
    }

    @Test(invocationCount = 1, threadPoolSize = 2, priority = 3)
    public void testGet() {
        Assert.assertNotNull(cache.get(5));
        Assert.assertEquals("色啊付款金额啊哈1", cache.get(1));
        Assert.assertNull(cache.get(3));
        Assert.assertEquals(((String[]) cache.get(6)).length, 3);
        Assert.assertEquals("沃尔夫大商股份", cache.get(14));
        Assert.assertEquals("杀人和渗透疗法", cache.get(15));
    }

    @Test
    public void testHget() {
    }

    @Test
    public void testHdel() {
    }

    @Test
    public void testDel() {
    }

    @Test
    public void testClear() {
        cache.clear();
        Assert.assertNull(cache.get(5));
        Assert.assertEquals(cache.get(14, 15, 6, 1, 2, 11).size(), 0);
    }

    @Test(priority = 5)
    public void testClose() throws IOException {
        client.close();
    }
}