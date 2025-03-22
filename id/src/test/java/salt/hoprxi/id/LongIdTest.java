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

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;


/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2021-08-07
 */
public class LongIdTest {

    @Test(priority = 2)
    public void generate() throws InterruptedException {
        long[] a = new long[10000000];
        for (int i = 0; i < 10000000; i++)
            a[i] = LongId.generate();
        Assert.assertNotEquals(a[999999], 0);
        for (int i = 1; i < 9; i++)
            System.out.println(a[i * 3600]);
        System.out.println();
        for (int i = 1; i < 9; i++)
            System.out.println(LongId.generate());
        Thread.sleep(1);
        System.out.println();
        for (int i = 1; i < 9; i++)
            System.out.println(LongId.generate());
    }

    @Test
    public void timestamp() throws ParseException {
        System.out.println("Machine：" + MacHash.hash());
        System.out.println("Process id：" + Process.process());
        System.out.println("Recover time from Identity:" + LongId.timestamp(3635769212049930240L));
        System.out.println("Recover time from Identity:" + LongId.timestamp(62086387027525364L));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        System.out.println("simpleDateFormat 2015-03-26 00:00:00(UTC/GMT) to long:"
                + simpleDateFormat.parse("2015-03-26 00:00:00.000").getTime());
        System.out.println("LocalDateTime 2015-03-26 00:00:00(UTC/GMT+08:00) to long:"
                + LocalDateTime.parse("2015-03-26 00:00:00.000", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli());
        System.out.println("1427328000000L to date: " + Instant.ofEpochMilli(1427328000000L));
        System.out.println("2025-01-01 00:00:00(UTC/GMT+08:00) to long:"
                + LocalDateTime.parse("2025-01-01 00:00:00.000", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli());
        System.out.println("1970-01-01 00:00:00(UTC/GMT+08:00) to long:"
                + LocalDateTime.parse("1970-01-01 00:00:00.000", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli());
        System.out.println("now to long:" + Instant.now().toEpochMilli());


        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        String processName = runtimeMXBean.getName();
        long processId = Long.parseLong(processName.split("@")[0]);
        System.out.println("当前进程ID：" + processId);

        System.out.println((1 << 21 | 1 << 12 | 2097152) + ":" + (1 << 21 | 1 << 12 | 4096) + ":" + (1 << 21));
        System.out.println(2097152 & 0xFFF);
    }
}