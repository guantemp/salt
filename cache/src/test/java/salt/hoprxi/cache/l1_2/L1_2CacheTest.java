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

package salt.hoprxi.cache.l1_2;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import salt.hoprxi.cache.Cache;
import salt.hoprxi.cache.caffeine.CaffeineCache;
import salt.hoprxi.cache.redis.RedisCacheBuilder;
import salt.hoprxi.cache.redis.RedisClient;
import salt.hoprxi.cache.redis.lettuce.LettuceStandAloneRedisClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2022-08-03
 */
public class L1_2CacheTest {

    private static final CaffeineCache<Integer, Object> caffine = new CaffeineCache(ConfigFactory.load("cache").getConfig("public_example.caffeine"));

    private static Config config = ConfigFactory.load("cache").getConfig("public_example.l2");

    static {
        String provider = config.getString("provider");
        config = config.getConfig(provider + ".standAlone");
    }

    private static final RedisClient<Integer, Object> client = new LettuceStandAloneRedisClient("test", config);
    private static final Cache<Integer, Object> redis = new RedisCacheBuilder<Integer, Object>("test").client(client).build();

    private static final Cache<Integer, Object> cache = new L1_2Cache<>(caffine, redis);

    @Test
    public void testPut() {
        cache.put(1, "2色股份第三个1423");
        cache.put(1, "色啊付款金额啊哈1");
        cache.put(2, "环境恶化和速度就会回来告诉uyyesanmf积分");
        cache.put(5, "环的vbddlgkregsdg阿境恶化和速度就会回来告诉uyyesanmf积分");
        String[] s = {"俗话说得好渡水复渡水", "说的话是个射天狼巍峨特务俄问题打了个而社科", "我日我还是发香料"};
        cache.put(6, s);
        Map<Integer, String> map = new HashMap<>();
        map.put(11, "safdas");
        map.put(12, "asd阿凡达2");
        map.put(13, "a杀毒二货·12");
        map.put(14, "沃尔夫大商股份");
        map.put(15, "十多个和我过");
        cache.put(map);
    }

    @Test(priority = 1)
    public void testGet() throws InterruptedException {
        Assert.assertNull(cache.get(3));
        Assert.assertEquals("a杀毒二货·12", cache.get(13));
        Assert.assertEquals("豪放派", cache.get(17, k -> "豪放派"));
        Assert.assertEquals("豪放派", cache.get(17));

        List list = Arrays.stream(new int[]{4, 5, 6, 7, 8, 13, 15}).boxed().collect(Collectors.toList());//boxed 转换int到Integer,不能自动打包
        Assert.assertEquals(4, cache.get(list).size());
        Assert.assertEquals("环的vbddlgkregsdg阿境恶化和速度就会回来告诉uyyesanmf积分", cache.get(list).get(5));
        Assert.assertEquals(3, cache.get(4, 5, 6, 12).size());

        Map<Integer, String> value = new HashMap<>();
        value.put(4, "但是过来的");
        value.put(7, "杀掉他如何沙滩和哇");
        value.put(8, "而且还有两天哈根达斯活动");

        Map<Integer, Object> result = cache.get(list, (sets) -> {
            int capacity = (int) (sets.size() / 0.75 + 1);
            Map<Integer, Object> map = new HashMap<>(capacity);
            for (Integer k : sets) {
                map.put(k, value.get(k));
            }
            return map;
        });
        //System.out.println(result);
        Assert.assertEquals("杀掉他如何沙滩和哇", cache.get(7));
    }

    @Test(priority = 3)
    public void testEvict() {
        cache.evict(8);
        Assert.assertNull(cache.get(8));
        cache.evict(4, 7, 8);
        Assert.assertNull(cache.get(4));
        Assert.assertNull(cache.get(7));
    }

    @Test(priority = 4)
    public void testClear() {
        cache.clear();
        Assert.assertNull(cache.get(1));
        Assert.assertNull(cache.get(2));
    }
}