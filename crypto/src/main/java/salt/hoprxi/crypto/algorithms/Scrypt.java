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

package salt.hoprxi.crypto.algorithms;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * SCrypt implements the password-based key derivative function created by Colin Percival. It
 * follows the scrypt algorithm published by IETF (RFC 7914).
 *
 * @author amdelamar, Will Glozer
 * @see <a href="https://en.wikipedia.org/wiki/Scrypt">https://en.wikipedia.org/wiki/Scrypt</a>
 */
@Deprecated
public class Scrypt {
    private static final int DEFAULT_SALT_LENGTH = 32;
    //CPU/内存消耗参数，必须大于1, 是2的n次幂并且小于2^(128 * r / 8)
    private static final int COST = 16384;
    //块容量参数
    private static final int BLOCKSIZE = 8;
    //并行化参数，一个小于等于((2^32-1) * hLen) / MFLen的正整数，其中hLen为 32，MFlen是128 * r。（在ltc协议中 p = 1）
    private static final int PARALLEL = 2;

    private static int log2(int num) {
        int log = 0;
        if ((num & 0xffff0000) != 0) {
            num >>>= 16;
            log = 16;
        }
        if (num >= 256) {
            num >>>= 8;
            log += 8;
        }
        if (num >= 16) {
            num >>>= 4;
            log += 4;
        }
        if (num >= 4) {
            num >>>= 2;
            log += 2;
        }
        return log + (num >>> 1);
    }

    /**
     * @param plainText Password.
     * @param salt      Salt.
     * @param cost      Overall CPU/MEM cost parameter. 2^15 for testing, but 2^20 recommended.
     * @param blocksize Block size for each mixing loop (memory usage).
     * @param parallel  Parallelization to control the number of independent mixing loops.
     * @param length    Intended length of the derived key.
     * @return The derived key.
     * //* @throws NoSuchAlgorithmException when HMAC_SHA256 is not available.
     * @throws IllegalArgumentException when parameters invalid
     */
    private static byte[] scrypt(byte[] plainText, byte[] salt, int cost, int blocksize, int parallel, int length)
            throws GeneralSecurityException {
        if (cost < 2 || (cost & (cost - 1)) != 0)
            throw new IllegalArgumentException("Cost must be a power of 2 greater than 1");
        if (cost > Integer.MAX_VALUE / 128 / blocksize)
            throw new IllegalArgumentException("Parameter cost is too large");
        if (blocksize > Integer.MAX_VALUE / 128 / parallel)
            throw new IllegalArgumentException("Parameter blocksize is too large");

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(plainText, "HmacSHA256"));

        byte[] key = new byte[length];

        byte[] b1 = new byte[128 * blocksize * parallel];
        byte[] xy = new byte[256 * blocksize];
        byte[] v1 = new byte[128 * blocksize * cost];

        pbkdf2(mac, salt, 1, b1, parallel * 128 * blocksize);

        for (int i = 0; i < parallel; i++) {
            smix(b1, i * 128 * blocksize, blocksize, cost, v1, xy);
        }

        pbkdf2(mac, b1, 1, key, length);

        return key;
    }

    private static void smix(byte[] b1, int bi, int round, int cpu, byte[] v1, byte[] xy) {
        int xi = 0;
        int yi = 128 * round;

        System.arraycopy(b1, bi, xy, xi, 128 * round);

        for (int i = 0; i < cpu; i++) {
            System.arraycopy(xy, xi, v1, i * (128 * round), 128 * round);
            blockMixSalsa8(xy, xi, yi, round);
        }

        for (int i = 0; i < cpu; i++) {
            int j = integerify(xy, xi, round) & (cpu - 1);
            blockxor(v1, j * (128 * round), xy, xi, 128 * round);
            blockMixSalsa8(xy, xi, yi, round);
        }

        System.arraycopy(xy, xi, b1, bi, 128 * round);
    }

    private static void blockMixSalsa8(byte[] by, int bi, int yi, int round) {

        byte[] x1 = new byte[64];
        System.arraycopy(by, bi + (2 * round - 1) * 64, x1, 0, 64);

        for (int i = 0; i < 2 * round; i++) {
            blockxor(by, i * 64, x1, 0, 64);
            salsa(x1);
            System.arraycopy(x1, 0, by, yi + (i * 64), 64);
        }

        for (int i = 0; i < round; i++) {
            System.arraycopy(by, yi + (i * 2) * 64, by, bi + (i * 64), 64);
        }

        for (int i = 0; i < round; i++) {
            System.arraycopy(by, yi + (i * 2 + 1) * 64, by, bi + (i + round) * 64, 64);
        }
    }

    private static int r1(int left, int right) {
        return (left << right) | (left >>> (32 - right));
    }

    private static void salsa(byte[] b1) {

        int[] base32 = new int[16];
        for (int i = 0; i < 16; i++) {
            base32[i] = (b1[i * 4] & 0xff);
            base32[i] |= (b1[i * 4 + 1] & 0xff) << 8;
            base32[i] |= (b1[i * 4 + 2] & 0xff) << 16;
            base32[i] |= (b1[i * 4 + 3] & 0xff) << 24;
        }

        int[] x1 = new int[16];
        System.arraycopy(base32, 0, x1, 0, 16);

        for (int i = 8; i > 0; i -= 2) {
            x1[4] ^= r1(x1[0] + x1[12], 7);
            x1[8] ^= r1(x1[4] + x1[0], 9);
            x1[12] ^= r1(x1[8] + x1[4], 13);
            x1[0] ^= r1(x1[12] + x1[8], 18);
            x1[9] ^= r1(x1[5] + x1[1], 7);
            x1[13] ^= r1(x1[9] + x1[5], 9);
            x1[1] ^= r1(x1[13] + x1[9], 13);
            x1[5] ^= r1(x1[1] + x1[13], 18);
            x1[14] ^= r1(x1[10] + x1[6], 7);
            x1[2] ^= r1(x1[14] + x1[10], 9);
            x1[6] ^= r1(x1[2] + x1[14], 13);
            x1[10] ^= r1(x1[6] + x1[2], 18);
            x1[3] ^= r1(x1[15] + x1[11], 7);
            x1[7] ^= r1(x1[3] + x1[15], 9);
            x1[11] ^= r1(x1[7] + x1[3], 13);
            x1[15] ^= r1(x1[11] + x1[7], 18);
            x1[1] ^= r1(x1[0] + x1[3], 7);
            x1[2] ^= r1(x1[1] + x1[0], 9);
            x1[3] ^= r1(x1[2] + x1[1], 13);
            x1[0] ^= r1(x1[3] + x1[2], 18);
            x1[6] ^= r1(x1[5] + x1[4], 7);
            x1[7] ^= r1(x1[6] + x1[5], 9);
            x1[4] ^= r1(x1[7] + x1[6], 13);
            x1[5] ^= r1(x1[4] + x1[7], 18);
            x1[11] ^= r1(x1[10] + x1[9], 7);
            x1[8] ^= r1(x1[11] + x1[10], 9);
            x1[9] ^= r1(x1[8] + x1[11], 13);
            x1[10] ^= r1(x1[9] + x1[8], 18);
            x1[12] ^= r1(x1[15] + x1[14], 7);
            x1[13] ^= r1(x1[12] + x1[15], 9);
            x1[14] ^= r1(x1[13] + x1[12], 13);
            x1[15] ^= r1(x1[14] + x1[13], 18);
        }

        for (int i = 0; i < 16; ++i) {
            base32[i] = x1[i] + base32[i];
        }

        for (int i = 0; i < 16; i++) {
            b1[i * 4] = (byte) (base32[i] & 0xff);
            b1[i * 4 + 1] = (byte) (base32[i] >> 8 & 0xff);
            b1[i * 4 + 2] = (byte) (base32[i] >> 16 & 0xff);
            b1[i * 4 + 3] = (byte) (base32[i] >> 24 & 0xff);
        }
    }

    private static void blockxor(byte[] s1, int si, byte[] d1, int di, int length) {
        for (int i = 0; i < length; i++) {
            d1[di + i] ^= s1[si + i];
        }
    }

    private static int integerify(byte[] b1, int bi, int round) {
        bi += (2 * round - 1) * 64;
        int n = (b1[bi] & 0xff);
        n |= (b1[bi + 1] & 0xff) << 8;
        n |= (b1[bi + 2] & 0xff) << 16;
        n |= (b1[bi + 3] & 0xff) << 24;

        return n;
    }

    /**
     * Implementation of PBKDF2 (RFC2898).
     *
     * @param mac        Pre-initialized {@link Mac} instance to use.
     * @param salt       Salt.
     * @param iterations Iteration count.
     * @param key        Byte array that derived key will be placed in.
     * @param length     Intended length, in octets, of the derived key.
     * @throws GeneralSecurityException If key length is too long
     */
    protected static void pbkdf2(Mac mac, byte[] salt, int iterations, byte[] key, int length) throws GeneralSecurityException {
        int len = mac.getMacLength();

        byte[] u1 = new byte[len];
        byte[] t1 = new byte[len];
        byte[] block = new byte[salt.length + 4];

        int limit = (int) Math.ceil((double) length / len);
        int r = length - (limit - 1) * len;

        System.arraycopy(salt, 0, block, 0, salt.length);

        for (int i = 1; i <= limit; i++) {
            block[salt.length] = (byte) (i >> 24 & 0xff);
            block[salt.length + 1] = (byte) (i >> 16 & 0xff);
            block[salt.length + 2] = (byte) (i >> 8 & 0xff);
            block[salt.length + 3] = (byte) (i & 0xff);

            mac.update(block);
            mac.doFinal(u1, 0);
            System.arraycopy(u1, 0, t1, 0, len);

            for (int j = 1; j < iterations; j++) {
                mac.update(u1);
                mac.doFinal(u1, 0);

                for (int k = 0; k < len; k++) {
                    t1[k] ^= u1[k];
                }
            }

            System.arraycopy(t1, 0, key, (i - 1) * len, (i == limit ? r : len));
        }
    }

    /**
     * Compare the supplied plaintext password to a hashed password.
     *
     * @param plainText            Plaintext password.
     * @param securedPlainTextHash scrypt hashed password.
     * @return true if plainText matches hashed value.
     * @throws IllegalStateException If JVM doesn't support necessary functions.
     */
    public static boolean verify(String plainText, String securedPlainTextHash) throws IllegalStateException {
        try {
            String[] parts = securedPlainTextHash.split("\\$");

            if (parts.length != 5 || !parts[1].equals("s0")) {
                throw new IllegalArgumentException("Invalid hashed value");
            }

            long params = Long.parseLong(parts[2], 16);
            byte[] salt = Base64.getDecoder().decode(parts[3]);
            byte[] derived = Base64.getDecoder().decode(parts[4]);

            int cost = (int) Math.pow(2, params >> 16 & 0xffff);
            int blockSize = (int) params >> 8 & 0xff;
            int parallel = (int) params & 0xff;

            byte[] derivedPwd = Scrypt.scrypt(plainText.getBytes(StandardCharsets.UTF_8), salt, cost, blockSize, parallel, 32);

            if (derived.length != derivedPwd.length) {
                return false;
            }

            // slow equals
            return slowEquals(derived, derivedPwd);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("JVM doesn't support SHA1PRNG or HMAC_SHA256?");
        }
    }

    /**
     * Compares two byte arrays.
     *
     * @param byteA First byte array.
     * @param byteB Second byte array.
     * @return true if they are equivalent.
     */
    private static boolean slowEquals(byte[] byteA, byte[] byteB) {
        int diff = byteA.length ^ byteB.length;
        for (int i = 0; i < byteA.length && i < byteB.length; i++) {
            diff |= byteA[i] ^ byteB[i];
        }
        return diff == 0;
    }

    /**
     * Creates a Hash from the given plainText using the specified algorithm.
     *
     * @param plainText  plainText
     * @param saltLength The salt byte length.
     * @param cost       Overall CPU/MEM cost parameter. 2^15 for testing, but 2^20 recommended.
     * @return The hashed password.
     * @throws IllegalStateException If JVM doesn't support necessary functions.
     */
    public static String scrypt(String plainText, int saltLength, int cost) throws IllegalStateException {
        return scrypt(plainText, saltLength, cost, BLOCKSIZE, PARALLEL);
    }

    /**
     * Creates a Hash from the given plainText using the specified algorithm.
     *
     * @param plainText plainText
     * @throws IllegalStateException If JVM doesn't support necessary functions.
     */
    public static String scrypt(String plainText) {
        return scrypt(plainText, DEFAULT_SALT_LENGTH, COST, BLOCKSIZE, PARALLEL);
    }

    /**
     * Hash the supplied plaintext plainText and generate output in the format described
     *
     * @param plainText  plainText.
     * @param saltLength The salt byte length default is 16.
     * @param cost       Overall CPU/MEM cost parameter. 2^15 for testing, but 2^20 recommended.
     * @param blockSize  Block size for each mixing loop (memory usage) default is 8
     * @param parallel   Parallelization to control the number of independent mixing loops,default is 1.
     * @return The hashed plainText.
     * @throws IllegalStateException If JVM doesn't support necessary functions.
     */
    public static String scrypt(String plainText, int saltLength, int cost, int blockSize, int parallel) throws IllegalStateException {
        try {
            byte[] salt = randomSalt(new SecureRandom(), saltLength);
            byte[] derived = scrypt(plainText.getBytes(StandardCharsets.UTF_8), salt, cost, blockSize, parallel, 32);
            String params = Long.toString((long) log2(cost) << 16L | (long) blockSize << 8 | parallel, 16);

            return "$s0$" + params + '$' +
                    Base64.getEncoder().encodeToString(salt) +
                    '$' +
                    Base64.getEncoder().encodeToString(derived);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("JVM doesn't support SHA1PRNG or HMAC_SHA256?");
        }
    }

    /**
     * @param secureRandom SecureRandom.getInstance("SHA1PRNG") or new SecureRandom use NativePRNG,will spend more time
     */
    private static byte[] randomSalt(SecureRandom secureRandom, int size) {
        byte[] salt = new byte[size];
        secureRandom.nextBytes(salt);
        return salt;
    }
}
