/*
 * Copyright (c) 2024. www.hoprxi.com All Rights Reserved.
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
 *   1bit          42bits                6bits               3bits        12bits
 * }</pre>
 * <li>use the 42-bits identification milliseconds time(support 139 year)
 * <li>6-bit(2^6=64) slice logo
 * <li>3-bit(2^3=8) process id
 * <li>12-bit(2^12=4096) sequence
 * </p>
 *
 * @author <a href="https://www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @version 0.0.6 2024-12-23
 * @since JDK8.0
 */
public class LongId {
    private static final int MACHINE_MASK = 0x3F;
    private static final int MACHINE_LEFT_SHIFT = 6;//机器码
    private static final int PROCESS = Process.process();     //Process id
    private static final int SEQUENCE_MASK = 0xFFF;
    private static final int SEQUENCE_LEFT_SHIFT = 12;
    private static final int PROCESS_MASK = 0x7;
    private static final int PROCESS_LEFT_SHIFT = 3;//进程码
    // This is begun from 2024-01-01 00:00:00(2015-03-26 00:00:00(UTC/GMT+08:00) = 1427328000000l)
    // This is begun from 2024-01-01 00:00:00(2025-01-01 00:00:00(UTC/GMT+08:00) = 1735689600000l)
    private static final long START = 1735689600000L;
    private static final int TIMESTIAMP_LEFT_SHIFT = MACHINE_LEFT_SHIFT + PROCESS_LEFT_SHIFT + SEQUENCE_LEFT_SHIFT;
    //may be use ThreadLocalRandom.current().nextInt() as initialValue
    private static AtomicInteger sequence = new AtomicInteger(ThreadLocalRandom.current().nextInt());
    private static long lastTimestamp = START;

    /**
     * Next id long.
     *
     * @return the long
     * @throws ClockCallbackException if Clock moved backwards from 2024-01-01 00:00:00
     */
    public static long generate() {
        long time = Instant.now().toEpochMilli() - START;
        if (time < lastTimestamp - START) {
            throw new ClockCallbackException(String.format(
                    "Clock moved backwards %d milliseconds.Refusing to generate id for %d milliseconds", START, time));
        }
        //AtomicInteger sequence = new AtomicInteger(ThreadLocalRandom.current().nextInt());
        int increment = sequence.getAndIncrement();
        if (increment == Integer.MAX_VALUE) {
            sequence = new AtomicInteger(ThreadLocalRandom.current().nextInt());
            time = tilNextMillis(lastTimestamp);
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
     * @param id id value
     * @return time of ID generation
     */
    public static LocalDateTime timestamp(long id) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli((id >> TIMESTIAMP_LEFT_SHIFT) + START), ZoneId.systemDefault());
    }

    /**
     * Waiting for the next millisecond
     *
     * @param lastTimestamp time of ID generation
     * @return a new timestamp
     */
    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = Instant.now().toEpochMilli() - START;
        while (timestamp <= lastTimestamp) {
            timestamp = Instant.now().toEpochMilli() - START;
        }
        return timestamp;
    }
}
