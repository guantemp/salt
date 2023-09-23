/*
 * Copyright (c) 2022. www.hoprxi.com All Rights Reserved.
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

import salt.hoprxi.to.ByteToHex;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;

/***
 * @author <a href="www.hoprxi.com/authors/guan xianghuang">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2019-07-14
 */
public final class ObjectId {
    private static final AtomicInteger SEQUENCE = new AtomicInteger(new SecureRandom().nextInt());
    private static final int MAC = MacHash.hash();
    private static final int PROCESS = Process.process();
    // This is begin from 2015-03-26 00:00:00(UTC/GMT+08:00)
    private static final long START = 1514764800000l;
    private static long lastTimestamp = START;
    private byte[] bytes = new byte[12];

    /**
     * Instantiates a new Object id.
     *
     * @throws ClockCallbackException if Clock moved backwards from 2015-03-26 00:00:00
     */
    public ObjectId() {
        this.generate();
    }

    public ObjectId generate() {
        long time = System.currentTimeMillis() - START;
        if (time < lastTimestamp - START) {
            throw new ClockCallbackException(String.format(
                    "Clock moved backwards.Refusing to generate id for %d milliseconds", time));
        }
        lastTimestamp = time;
        bytes[0] = (byte) (0xff & time >> 40);
        bytes[1] = (byte) (0xff & time >> 32);
        bytes[2] = (byte) (0xff & time >> 24);
        bytes[3] = (byte) (0xff & time >> 16);
        bytes[4] = (byte) (0xff & time >> 8);
        bytes[5] = (byte) (0xff & time);

        bytes[6] = (byte) (0xff & MAC >> 8);
        bytes[7] = (byte) (0xff & MAC);

        bytes[8] = (byte) (0xff & PROCESS >> 8);
        bytes[9] = (byte) (0xff & PROCESS);

        int increment = SEQUENCE.getAndIncrement();
        bytes[10] = (byte) (0xff & increment >> 8);
        bytes[11] = (byte) (0xff & increment);
        return this;
    }

    public String id() {
        return ByteToHex.toHexStr(bytes);
    }

    public LocalDateTime timestamp() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
        byteBuffer.put(new byte[]{(byte) 0, (byte) 0, bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5]});
        byteBuffer.flip();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(byteBuffer.getLong() + START), ZoneId.systemDefault());
    }
}
