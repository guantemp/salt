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

package salt.hoprxi.id;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;


/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2021-08-07
 */
public class LongIdTest {

    @Test
    public void generate() throws InterruptedException {
        long[] a = new long[10000000];
        for (int i = 0; i < 10000000; i++)
            a[i] = LongId.generate();
        Assert.assertNotEquals(a[999999], 0);
        for (int i = 1; i < 99; i++)
            System.out.println(LongId.generate());
        Thread.sleep(100);
        System.out.println();
        System.out.println();
        for (int i = 1; i < 99; i++)
            System.out.println(LongId.generate());
        Thread.sleep(100);
        System.out.println();
        for (int i = 1; i < 9; i++)
            System.out.println(a[i * 360]);
    }

    @Test
    public void timestamp() throws ParseException {
        System.out.println("2015-03-26 00:00:00(UTC/GMT+08:00) to long:"
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2015-03-26 00:00:00.000").getTime());
        System.out.println("1427299200000L to date: " + Instant.ofEpochMilli(1427328000000L));
        System.out.println("Machine：" + MacHash.hash());
        System.out.println("Process id：" + Process.process());
        System.out.println("Recover time from Identity:" + LongId.timestamp(LongId.generate()));
        System.out.println("2023-01-01 00:00:00(UTC/GMT+08:00) to long:"
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2023-01-01 00:00:00.000").getTime());
        System.out.println("1970-01-01 00:00:00(UTC/GMT+08:00) to long:"
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("1970-01-01 08:00:00.000").getTime());
        System.out.println("now to long:" + Instant.now().toEpochMilli());
    }
}