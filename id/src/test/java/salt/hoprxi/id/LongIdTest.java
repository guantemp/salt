/*
 * Copyright (c) 2021. www.hoprxi.com All Rights Reserved.
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

package salt.hoprxi.id;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 2019-07-15
 */
public class LongIdTest {

    @Test
    public void generate() {
        long[] a = new long[10000000];
        for (int i = 0; i < 10000000; i++)
            a[i] = LongId.generate();
        Assert.assertNotEquals(a[999999], 0);
        //for (int i = 999999; i > 99899; i--)
        // System.out.println(a[i]);
    }

    @Test
    public void generate1() {
        System.out.println(LongId.generate());
    }

    @Test
    public void timestamp() throws ParseException {
        System.out.println("Recover time from Identity:" + LongId.timestamp(LongId.generate()));
        System.out.println("2021-01-01 00:00:00(UTC/GMT+08:00) to long:"
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2021-05-01 00:00:00.000").getTime());
    }
}