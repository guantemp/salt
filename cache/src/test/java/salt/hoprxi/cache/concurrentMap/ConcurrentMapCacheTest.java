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

package salt.hoprxi.cache.concurrentMap;


import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import salt.hoprxi.cache.Cache;

import java.util.concurrent.TimeUnit;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.2 2022-07-12
 */
@Deprecated
public class ConcurrentMapCacheTest {
    private static final Cache<Integer, String> cache = new ConcurrentMapCacheBuilder<Integer, String>("test").expired(1, TimeUnit.SECONDS).maxAmount(4).maximumSize(160).build();

    @BeforeClass
    public void beforeClass() {
        cache.put(1, "2色股份第三个1423");
        cache.put(1, "色啊付款金额啊哈");
        cache.put(2, "环境恶化和速度就会回来告诉uyyesanmf积分");
        cache.put(5, "环的vbddlgkregsdg阿境恶化和速度就会回来告诉uyyesanmf积分");

    }

    @Test(dependsOnMethods = {"putAndGet"})
    public void evictAndClear() {
        Cache<Integer, String> temp = new ConcurrentMapCacheBuilder<Integer, String>("temp").build();
        temp.put(1, "sdjk");
        temp.put(2, "sdjk");
        temp.put(3, "sdjk");
        temp.put(4, "sdjk");
        temp.put(5, "sdjk");
        temp.put(6, "sdjk");
        temp.put(7, "sdjk");
        //System.out.println(temp.stats());
        temp.evict(4);
        //System.out.println(temp.stats());
        temp.clear();
        //System.out.println(temp.stats());
    }

    @Test
    public void putAndGet() throws InterruptedException {
        cache.put(1, "速度快很多是");
        cache.put(2, "书店看到了亚特兰模式对话框");
        cache.put(3, "的认识了对方是一个人");
        cache.put(4, "塞得港省大");
        Assert.assertNull(cache.get(4));
        Assert.assertNotNull(cache.get(3));
        //System.out.println(cache.stats());
        //Thread.sleep(1010);
        cache.put(4, "第六十一条好地方犹太人");
        cache.put(5, "行动提供了绘图仪");
        // Assert.assertNotNull(cache.value(4));
        //Assert.assertNull(cache.value(2));
        //System.out.println(cache.stats());
    }
}