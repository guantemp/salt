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

    // 【核心】：使用 AtomicLong 将“时间戳”和“序列号”合并存储，实现真正的无锁 CAS
    // 高 42 位存时间戳，低 22 位存序列号（足够容纳 4096 个序列）
    private static final AtomicLong STATE = new AtomicLong(0L);

    public static long generate() {
        long time = Instant.now().toEpochMilli() - START;

        while (true) {
            long currentState = STATE.get();
            // 提取高 42 位作为旧时间戳
            long oldTime = currentState >>> 22;
            // 提取低 22 位作为旧序列号
            long oldSeq = currentState & 0x3FFFFF;

            // 1. 时钟回拨检查
            if (time < oldTime) {
                long offset = oldTime - time;
                if (offset <= 5) {
                    // 轻微回拨：自旋等待
                    Thread.onSpinWait();
                    time = Instant.now().toEpochMilli() - START;
                    continue; // 重新进入循环
                } else {
                    // 严重回拨：直接抛出异常
                    throw new ClockCallbackException(String.format(
                            "Clock moved backwards severely. Refusing to generate id for %d milliseconds", offset));
                }
            }

            long newTime;
            long newSeq;

            if (time == oldTime) {
                // 2. 同一毫秒内，序列号自增
                newSeq = (oldSeq + 1) & SEQUENCE_MASK;
                if (newSeq == 0) {
                    // 序列号用尽，等待下一毫秒
                    newTime = tilNextMillis(oldTime);
                    newSeq = 0;
                } else {
                    newTime = time;
                }
            } else {
                // 3. 新的一毫秒，重置序列号
                newTime = time;
                newSeq = 0;
            }

            // 4. 【核心】：将新的时间戳和序列号合并成一个 long，进行 CAS 更新
            long newState = (newTime << 22) | newSeq;

            // 如果 CAS 成功，说明当前线程抢到了生成权，直接退出循环并组装 ID
            if (STATE.compareAndSet(currentState, newState)) {
                return (newTime << TIMESTAMP_LEFT_SHIFT) |
                       ((MacHash.hash() & MACHINE_MASK) << (PROCESS_LEFT_SHIFT + SEQUENCE_LEFT_SHIFT)) |
                       ((PROCESS & PROCESS_MASK) << SEQUENCE_LEFT_SHIFT) |
                       newSeq;
            }
            // 如果 CAS 失败，说明有其他线程先一步更新了 state，当前线程直接进入下一次 while 循环重试
        }
    }

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = Instant.now().toEpochMilli() - START;
        while (timestamp <= lastTimestamp) {
            Thread.onSpinWait();
            timestamp = Instant.now().toEpochMilli() - START;
        }
        return timestamp;
    }


    public static LocalDateTime timestamp(long id) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli((id >> TIMESTAMP_LEFT_SHIFT) + START), ZoneId.systemDefault());
    }
}
