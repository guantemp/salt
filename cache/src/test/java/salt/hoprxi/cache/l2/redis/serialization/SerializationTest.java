/*
 * Copyright (c) 2019 www.hoprxi.com All Rights Reserved.
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

package salt.hoprxi.cache.l2.redis.serialization;


import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.1 2019-04-03
 */
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SerializationTest {
    private static Serialization fst = new FSTSerialization();
    private static Serialization kryo = new KryoSerialization();
    private static List<String> list;
    private static String[] arry;
    private static Map<String, Double> map;

    @BeforeClass
    public static void setUp() throws Exception {
        list = new ArrayList<>();
        list.add("打个电话");
        list.add("打大股东个电话");

        arry = new String[]{"上海市上的", "大概范德法特", "独上高楼来了"};

        map = new HashMap();
        map.put("a", 100.0);
        map.put("b", 80.0);
        map.put("c", 50.0);
    }

    @Test
    public void fstSerializeAndDeserialize() {
        byte[] l = fst.serialize(list);
        List<String> listResult = fst.deserialize(l);
        Assert.assertEquals(2, listResult.size());

        byte[] a = fst.serialize(arry);
        String[] arryResult = fst.deserialize(a);
        Assert.assertEquals(3, arryResult.length);


        byte[] d = fst.serialize(map);
        Map<String, Double> mapResult = fst.deserialize(d);
        Assert.assertEquals(3, mapResult.size());
    }

    @Test
    public void kryoSerializeAndDeserialize() {
        byte[] d = kryo.serialize(map);
        Map<String, Double> mapResult = kryo.deserialize(d);
        Assert.assertEquals(3, mapResult.size());

        byte[] n = kryo.serialize(list);
        List<String> listResult = kryo.deserialize(n);
        Assert.assertEquals(2, listResult.size());

        byte[] b = kryo.serialize(arry);
        String[] arryResult = kryo.deserialize(b);
        Assert.assertEquals(3, arryResult.length);
    }
}