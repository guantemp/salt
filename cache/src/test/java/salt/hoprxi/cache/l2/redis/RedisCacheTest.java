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

package salt.hoprxi.cache.l2.redis;

import com.typesafe.config.ConfigFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import salt.hoprxi.cache.Cache;
import salt.hoprxi.cache.l2.redis.jedis.JedisClient;
import salt.hoprxi.cache.l2.redis.jedis.StandAloneJedisClient;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.1 2019-03-18
 */
public class RedisCacheTest {
    private static JedisClient<Integer, Object> client = new StandAloneJedisClient(ConfigFactory.load("cache"));
    private static Cache<Integer, Object> redisCache = new RedisCacheBuilder<Integer, Object>("test").client(client).build();

    static {
        redisCache.put(1, "2色股份第三个1423");
        redisCache.put(1, "色啊付款金额啊哈");
        redisCache.put(2, "环境恶化和速度就会回来告诉uyyesanmf积分");
        redisCache.put(5, "环的vbddlgkregsdg阿境恶化和速度就会回来告诉uyyesanmf积分");
        String[] s = {"俗话说得好渡水复渡水", "说的话是个射天狼巍峨特务俄问题打了个而社科", "我日我还是发香料"};
        redisCache.put(6, s);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        redisCache.evict(1);
        redisCache.evict(2);
        redisCache.evict(5);
        redisCache.evict(6);
    }

    @Test
    public void clear() {
    }

    @Test
    public void get() {
        Assert.assertNotNull(redisCache.get(5));
        String[] ss = (String[]) redisCache.get(6);
        for (String s : ss)
            System.out.println(s);
        Assert.assertEquals("色啊付款金额啊哈", redisCache.get(1));
        Assert.assertNull(redisCache.get(3));
        Assert.assertNotNull(redisCache.get(6));
    }

    @Test
    public void region() {
        //Assert.assertNotNull(redisCache.region());
    }

    @Test
    public void evict() {
        redisCache.evict(2);
        redisCache.evict(21);
    }

    @Test
    public void stats() {
        //System.out.println(redisCache.stats());
    }
}