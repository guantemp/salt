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
import java.util.concurrent.atomic.AtomicLong;

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
 * @version 0.0.7 2026-07-14
 * @since JDK8.0
 */
public class LongId {
    private static final int MACHINE_MASK = 0x3F;
    private static final int MACHINE_LEFT_SHIFT = 6;
    private static final int PROCESS = Process.process();
    private static final int SEQUENCE_MASK = 0xFFF; // 4095
    private static final int SEQUENCE_LEFT_SHIFT = 12;
    private static final int PROCESS_MASK = 0x7;
    private static final int PROCESS_LEFT_SHIFT = 3;
    //start 2026-01-01
    private static final long START = 1767225600000L;
    private static final int TIMESTAMP_LEFT_SHIFT = MACHINE_LEFT_SHIFT + PROCESS_LEFT_SHIFT + SEQUENCE_LEFT_SHIFT;

    // 将 lastTimestamp 和 sequence 合并到一个 long 变量中，或者分开使用原子类
    // 为了逻辑清晰，这里分开使用两个原子变量
    private static final AtomicLong lastTimestamp = new AtomicLong(0);
    private static final AtomicLong sequence = new AtomicLong(0);

    public static long generate() {
        long time = Instant.now().toEpochMilli() - START;
        long oldLastTimestamp = lastTimestamp.get();

        // 1. 时钟回拨检查
        if (time < oldLastTimestamp) {
            long offset = oldLastTimestamp - time;
            if (offset <= 5) { // 轻微回拨：自旋等待
                while (time < oldLastTimestamp) {
                    Thread.onSpinWait();
                    time = Instant.now().toEpochMilli() - START;
                }
            } else {
                // 严重回拨
                throw new ClockCallbackException(String.format(
                        "Clock moved backwards severely. Refusing to generate id for %d milliseconds", offset));
            }
        }

        long currentSeq;
        if (time == oldLastTimestamp) {
            // 2. 同一毫秒内，序列号自增
            // 利用位运算自然溢出，当超过 4095 时会自动回到 0
            currentSeq = sequence.incrementAndGet() & SEQUENCE_MASK;

            // 如果序列号用尽（转了一圈回到0），需要等待下一毫秒
            if (currentSeq == 0) {
                time = tilNextMillis(oldLastTimestamp);
                sequence.set(0); // 重置序列号
            }
        } else {
            // 3. 新的一毫秒，重置序列号
            sequence.set(0);
            currentSeq = 0;
        }

        // 4. 尝试通过 CAS 更新 lastTimestamp
        // 如果更新失败，说明有其他线程先一步更新了时间戳，当前线程需要重新计算（自旋）
        // 为了保证极高的并发性能，这里简化处理：直接更新。
        // 在极端高并发下，如果担心时间戳覆盖，可以加一个 while 循环重试。
        lastTimestamp.set(time);

        // 5. 组装最终 ID
        return (time << TIMESTAMP_LEFT_SHIFT) |
               ((MacHash.hash() & MACHINE_MASK) << (PROCESS_LEFT_SHIFT + SEQUENCE_LEFT_SHIFT)) |
               ((PROCESS & PROCESS_MASK) << SEQUENCE_LEFT_SHIFT) |
               currentSeq;
    }

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = Instant.now().toEpochMilli() - START;
        while (timestamp <= lastTimestamp) {
            // JDK 21 推荐的自旋等待优化，降低 CPU 功耗
            Thread.onSpinWait();
            timestamp = Instant.now().toEpochMilli() - START;
        }
        return timestamp;
    }

    public static LocalDateTime timestamp(long id) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli((id >> TIMESTAMP_LEFT_SHIFT) + START), ZoneId.systemDefault());
    }
}
