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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Generate of a long-type primary key,the total bits is 63
 * <pre>{@code
 * +------+------------------------+------------------+--------------+------------+
 * | sign |   delta milliseconds   |  mac hash code   |   process id |  sequence  |
 * +------+------------------------+------------------+--------------+------------+
 *   1bit          42bits                5bits               5bit         11bits
 * }</pre>
 * <li>use the 42-bit identification milliseconds time(support 139 year)
 * <li>5-bit(32) slice logo
 * <li>5-bit(32) process
 * <li>11-bit(2048) sequence
 * </p>
 *
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @version 0.0.5 2024-02-14
 * @since JDK8.0
 */
public class LongId {
    private static final int MACHINE_MASK = 0x1F;
    private static final int MACHINE_LEFT_SHIFT = 5;
    //Process id
    private static final int PROCESS = Process.process();
    private static final int SEQUENCE_MASK = 0x7FF;
    private static final int SEQUENCE_LEFT_SHIFT = 11;
    private static final int PROCESS_MASK = 0x1F;
    private static final int PROCESS_LEFT_SHIFT = 5;
    // This is begin from 2023-01-01 00:00:00(2015-03-26 00:00:00(UTC/GMT+08:00) -1427328000000)
    private static final long START = 1704067200000L;
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
                    "Clock moved backwards %d milliseconds.Refusing to generate id for %d milliseconds", START, time));
        }
        //AtomicInteger sequence = new AtomicInteger(ThreadLocalRandom.current().nextInt());
        int increment = sequence.getAndIncrement();
        if (increment == Integer.MAX_VALUE)
            synchronized (LongId.class) {
                sequence = new AtomicInteger(ThreadLocalRandom.current().nextInt());
            }
        if ((increment & SEQUENCE_MASK) == 0 && lastTimestamp == time) {
            time = tilNextMillis(lastTimestamp);
        }
        lastTimestamp = time;
        return time << TIMESTIAMP_LEFT_SHIFT | (MacHash.hash() & MACHINE_MASK) << (PROCESS_LEFT_SHIFT + SEQUENCE_LEFT_SHIFT) | (PROCESS & PROCESS_MASK) << SEQUENCE_LEFT_SHIFT | (increment & SEQUENCE_MASK);
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
