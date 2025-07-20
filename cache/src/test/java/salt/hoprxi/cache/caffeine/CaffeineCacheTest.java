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

package salt.hoprxi.cache.caffeine;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2022-07-01
 */
public class CaffeineCacheTest {
    private static final CaffeineCache<Integer, String> cache;
    static {
        Config config = ConfigFactory.load("cache");
        System.out.println(config.getConfig("caffeine").getString("max-size"));
        cache = new CaffeineCache<>(config);
    }


    @BeforeClass
    public void beforeClass() {
        cache.put(1, "2色股份第三个1423");
        cache.put(1, "色啊付款金额啊哈");
        cache.put(2, "环境恶化和速度就会回来告诉uyyesanmf积分");
        cache.put(5, "环的vbddlgkregsdg阿境恶化和速度就会回来告诉uyyesanmf积分");
    }

    @Test(invocationCount = 2, expectedExceptions = {NullPointerException.class})
    public void testPut() {
        cache.put(6, "好老六");
        Map<Integer, String> map = new HashMap<>();
        map.put(11, "俗话说得好渡水复渡水");
        map.put(12, "说的话是个射天狼巍峨特务俄问题打了个而社科");
        map.put(13, "我日我还是发香料");
        cache.put(map);
        cache.put(25, null);
    }

    @Test(dependsOnMethods = {"testPut"})
    public void testGet() {
        Assert.assertEquals("好老六", cache.get(6));
        List<Integer> list = Arrays.stream(new int[]{4, 5, 6}).boxed().collect(Collectors.toList());//boxed 转换int到Integer,不能自动打包
        Assert.assertEquals(cache.get(list).size(), 2);
        Assert.assertEquals(cache.get(list).get(6), "好老六");
        Assert.assertEquals(cache.get(4, 5, 6, 12).size(), 3);
        Assert.assertEquals(cache.get(13), "我日我还是发香料");
        cache.get(14, k -> "豪放派");
        Assert.assertEquals(cache.get(14), "豪放派");
    }

    @Test(dependsOnMethods = {"testGet"})
    public void testEvict() {
        cache.evict(13);
        Assert.assertNull(cache.get(13));
        cache.evict(1);
        Assert.assertNull(cache.get(1));
    }


    @Test(priority = 4)
    public void testClear() {
        cache.clear();
        Assert.assertNull(cache.get(6));
        Assert.assertNull(cache.get(12));
    }
}