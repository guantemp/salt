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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Generate of a long-type primary key,the total bits is 63
 * <pre>{@code
 * +------+------------------------+------------------+--------------+------------+
 * | sign |   delta milliseconds   |  mac hash code   |   process id |  sequence  |
 * +------+------------------------+------------------+--------------+------------+
 *   1bit          43bits                9bits               1bit         10bits
 * }</pre>
 * <li>use the 43-bit identification milliseconds time(support 138 year)
 * <li>9-bit(512) slice logo
 * <li>1-bit(2) process
 * <li>10-bit(1024) sequence
 * </p>
 *
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @version 0.0.3 2020-09-01
 * @since JDK8.0
 */
public class LongId {
    private static final int MACHINE_MASK = 0x1FF;
    private static final int MACHINE_LEFT_SHIFT = 9;
    //Process id
    private static final int PROCESS = Process.process();
    private static final int SEQUENCE_MASK = 0x3FF;
    private static final int SEQUENCE_LEFT_SHIFT = 10;
    private static final int PROCESS_MASK = 0x1;
    private static final int PROCESS_LEFT_SHIFT = 1;
    // This is begin from 2015-03-26 00:00:00(UTC/GMT+08:00) --1427328000000
    private static final long START = 1619798400000l;
    private static final int TIMESTIAMP_LEFT_SHIFT = MACHINE_LEFT_SHIFT + PROCESS_LEFT_SHIFT + SEQUENCE_LEFT_SHIFT;
    //may be use ThreadLocalRandom.current().nextInt() as initialValue
    private static AtomicInteger sequence = new AtomicInteger(0);
    private static long lastTimestamp = START;

    /**
     * Next id long.
     *
     * @return the long
     * @throws ClockCallbackException if Clock moved backwards from 2015-03-26 00:00:00
     */
    public static long generate() {
        long time = Instant.now().toEpochMilli() - START;
        if (time < lastTimestamp - START) {
            throw new ClockCallbackException(String.format(
                    "Clock moved backwards.Refusing to generate id for %d milliseconds", time));
        }
        int increment = sequence.getAndIncrement();
        if (increment == Integer.MAX_VALUE)
            synchronized (LongId.class) {
                sequence = new AtomicInteger(0);
            }
        if ((increment & SEQUENCE_MASK) == 0 && lastTimestamp == time) {
            time = tilNextMillis(lastTimestamp);
        }
        lastTimestamp = time;
        return time << TIMESTIAMP_LEFT_SHIFT | (MacHash.hash() & MACHINE_MASK) << (PROCESS_LEFT_SHIFT + SEQUENCE_LEFT_SHIFT) | (PROCESS & PROCESS_MASK) << SEQUENCE_LEFT_SHIFT | (increment & SEQUENCE_MASK);
    }

    /**
     * @param args
     * @throws ParseException
     */
    public static void main(String[] args) throws ParseException {
        System.out.println(Instant.ofEpochMilli(1427328000000l));
        System.out.println("Machine：" + MacHash.hash());
        System.out.println("Process id：" + PROCESS);
        System.out.println("Recover time from Identity:" + timestamp(LongId.generate()));
        System.out.println("2019-01-01 08:00:00(UTC/GMT+08:00) to long:"
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2019-01-01 08:00:00.000").getTime());
    }

    /**
     * Get localDateTime for Identity generate value
     *
     * @param id
     * @return
     */
    public static LocalDateTime timestamp(long id) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli((id >> TIMESTIAMP_LEFT_SHIFT) + START), ZoneId.systemDefault());
    }

    /**
     * Waiting for the next millisecond
     *
     * @param lastTimestamp
     * @return
     */
    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = Instant.now().toEpochMilli() - START;
        while (timestamp <= lastTimestamp) {
            timestamp = Instant.now().toEpochMilli();
        }
        return timestamp;
    }
}
