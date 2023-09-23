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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2022-07-09
 */
public class CacheFactoryTest {

    @Test(invocationCount = 4, threadPoolSize = 2)
    public void testBuildCache() {
        Cache<Integer, String> cache = CacheFactory.build("test");
        cache.put(1, "2色股份第三个1423");
        cache.put(1, "色啊付款金额啊哈");
        cache.put(2, "环境恶化和速度就会回来告诉uyyesanmf积分");
        cache.put(5, "环的vbddlgkregsdg阿境恶化和速度就会回来告诉uyyesanmf积分");
        cache.put(6, "好老六");
        Map<Integer, String> map = new HashMap<>();
        map.put(11, "俗话说得好渡水复渡水");
        map.put(12, "说的话是个射天狼巍峨特务俄问题打了个而社科");
        map.put(13, "我日我还是发香料");
        cache.put(map);
        Assert.assertEquals("好老六", cache.get(6));
        List list = Arrays.stream(new int[]{4, 5, 6}).boxed().collect(Collectors.toList());//boxed 转换int到Integer
        Assert.assertEquals(2, cache.get(list).size());
        Assert.assertEquals("好老六", cache.get(list).get(6));
        Assert.assertEquals(3, cache.get(4, 5, 6, 12).size());
        Assert.assertEquals("我日我还是发香料", cache.get(13));
        cache.get(14, k -> "豪放派");
        Assert.assertEquals("豪放派", cache.get(14));

        cache = CacheFactory.build("test");
        Assert.assertEquals("好老六", cache.get(6));
        list = Arrays.stream(new int[]{4, 5, 6}).boxed().collect(Collectors.toList());//boxed 转换int到Integer
        Assert.assertEquals(2, cache.get(list).size());
        Assert.assertEquals("好老六", cache.get(list).get(6));
        Assert.assertEquals(3, cache.get(4, 5, 6, 12).size());
        Assert.assertEquals("我日我还是发香料", cache.get(13));
        cache.get(14, k -> "豪放派");
        Assert.assertEquals("豪放派", cache.get(14));

        Cache<Integer, String> test = CacheFactory.build("test");
        Assert.assertSame(test, cache);

        Cache<Integer, String> test1 = CacheFactory.build("test");
        Assert.assertSame(test1, cache);
    }

    @Test(invocationCount = 2)
    public void testTestBuildCache() {
        Cache<Long, String> cache = CacheFactory.build("smsCode");
        Cache<Long, String> cache1 = CacheFactory.build("smsCode");
        Assert.assertSame(cache, cache1);
        cache.put(1L, "2色股份第三个1423");
        Assert.assertEquals(cache1.get(1L), "2色股份第三个1423");
        Cache<Long, String> cache2 = CacheFactory.build("category_view");
        cache2.put(1L, " \"categories\" : [ {\n" +
                "    \"id\" : \"-1\",\n" +
                "    \"parentId\" : \"-1\",\n" +
                "    \"isLeaf\" : true,\n" +
                "    \"name\" : {\n" +
                "      \"name\" : \"未分类\",\n" +
                "      \"mnemonic\" : \"wfl\",\n" +
                "      \"alias\" : \"undefined\"\n" +
                "    },\n" +
                "    \"description\" : \"undefined category\"\n" +
                "  }]");
        System.out.println("region is category_view：\n" + cache2.get(1L));
        Cache<Long, String> cache3 = CacheFactory.build("item_view");
        System.out.println("region is item_view,it‘s l1_2：\n" + cache3);
        cache3.put(567l, "爱上灰色空间爱上了好热啊晚上");
        System.out.println(cache3.get(567l));
    }
}