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
 * @version 0.0.8 2026-07-29
 * @since JDK21
 */
public class LongId {
    private static final int MACHINE_MASK = 0x3F;
    private static final int PROCESS_MASK = 0x7;
    private static final int SEQUENCE_MASK = 0xFFF;

    // 位偏移定义（与最终ID布局完全一致）
    private static final int SEQ_SHIFT = 0;
    private static final int PROC_SHIFT = 12;
    private static final int MAC_SHIFT = 15;      // 12 + 3
    private static final int TIME_SHIFT = 21;     // 12 + 3 + 6

    private static final long START = 1790812800000L; // 2026-10-01

    // 【核心修复】STATE 直接存储最终 ID 的高 52 位（时间+机器+进程+序列）
    // 不再分离存储时间和序列，彻底消除解析歧义
    private static final AtomicLong STATE = new AtomicLong(0L);

    // 预计算固定的机器和进程标识，避免每次生成时重复计算
    private static final long FIXED_MAC_PROC_BITS =
            ((long)(MacHash.hash() & MACHINE_MASK) << MAC_SHIFT) |
            ((long)(Process.process() & PROCESS_MASK) << PROC_SHIFT);

    public static long generate() {
        while (true) {
            // 【关键】每次循环都获取最新时间，绝不使用过期缓存
            long now = Instant.now().toEpochMilli() - START;

            long currentState = STATE.get();
            long oldTime = currentState >>> TIME_SHIFT;
            long oldSeq = currentState & SEQUENCE_MASK;

            long newTime;
            long newSeq;

            if (now > oldTime) {
                // 新毫秒：时间前进，序列号归零
                newTime = now;
                newSeq = 0;
            } else if (now == oldTime) {
                // 同一毫秒：序列号自增
                newSeq = (oldSeq + 1) & SEQUENCE_MASK;
                if (newSeq == 0) {
                    // 序列号耗尽，必须等待下一毫秒
                    // 【关键修复】等待期间持续刷新时间，不使用进入方法时的旧时间
                    do {
                        Thread.onSpinWait();
                        now = Instant.now().toEpochMilli() - START;
                    } while (now <= oldTime);
                    newTime = now;
                    newSeq = 0;
                } else {
                    newTime = oldTime;
                }
            } else {
                // 时钟回拨
                long offset = oldTime - now;
                if (offset <= 5) {
                    Thread.onSpinWait();
                    continue; // 重新获取最新时间
                }
                throw new ClockCallbackException(
                        String.format("Clock moved backwards %d ms", offset));
            }

            // 【核心修复】新状态直接与最终ID布局对齐，无需二次重组
            long newState = (newTime << TIME_SHIFT) | FIXED_MAC_PROC_BITS | newSeq;

            if (STATE.compareAndSet(currentState, newState)) {
                // CAS 成功，newState 本身就是完整的 ID
                return newState;
            }
            // CAS 失败，下一轮循环自动获取最新时间重试
        }
    }

    public static LocalDateTime timestamp(long id) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli((id >>> TIME_SHIFT) + START),
                ZoneId.systemDefault()
        );
    }
    /*
     // ---------- 位分配常量 ----------
    private static final int MACHINE_MASK = 0x3F;          // 6位
    private static final int MACHINE_LEFT_SHIFT = 6;
    private static final int PROCESS_MASK = 0x7;           // 3位
    private static final int PROCESS_LEFT_SHIFT = 3;
    private static final int SEQUENCE_MASK = 0xFFF;        // 12位
    private static final int SEQUENCE_LEFT_SHIFT = 12;

    // 总左移位数 = 6 + 3 + 12 = 21
    private static final int TIMESTAMP_LEFT_SHIFT = MACHINE_LEFT_SHIFT + PROCESS_LEFT_SHIFT + SEQUENCE_LEFT_SHIFT;

    private static final long START = 1767225600000L;      // 2026-01-01

    // ---------- 机器/进程标识（需替换为实际实现） ----------
    private static final int MACHINE_ID = MacHash.hash() & MACHINE_MASK;
    private static final int PROCESS_ID = Process.process() & PROCESS_MASK;

    // ---------- 无锁状态 ----------
    private static final AtomicLong STATE = new AtomicLong(0L);

    public static long generate() {
        while (true) {
            // ★★★ 关键修复：每次循环都重新获取当前时间 ★★★
            long time = Instant.now().toEpochMilli() - START;

            long currentState = STATE.get();
            long oldTime = currentState >>> TIMESTAMP_LEFT_SHIFT;          // 正确右移21位
            long oldSeq = currentState & ((1L << TIMESTAMP_LEFT_SHIFT) - 1); // 低21位掩码

            // 时钟回拨检查
            if (time < oldTime) {
                long offset = oldTime - time;
                if (offset <= 5) {
                    Thread.onSpinWait();
                    continue;   // 重试，time 会重新获取
                } else {
                    throw new ClockCallbackException("Clock moved backwards: " + offset + " ms");
                }
            }

            long newTime, newSeq;
            if (time == oldTime) {
                newSeq = (oldSeq + 1) & SEQUENCE_MASK;
                if (newSeq == 0) {
                    newTime = tilNextMillis(oldTime);
                    newSeq = 0;
                } else {
                    newTime = time;
                }
            } else {
                newTime = time;
                newSeq = 0;
            }

            long newState = (newTime << TIMESTAMP_LEFT_SHIFT) | newSeq;
            if (STATE.compareAndSet(currentState, newState)) {
                return (newTime << TIMESTAMP_LEFT_SHIFT)
                        | ((long) MACHINE_ID << (PROCESS_LEFT_SHIFT + SEQUENCE_LEFT_SHIFT))
                        | ((long) PROCESS_ID << SEQUENCE_LEFT_SHIFT)
                        | newSeq;
            }
            // CAS 失败，循环重试，time 会在下一轮重新获取
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
     */
}

