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
package salt.hoprxi.crypto.hash;


import org.bouncycastle.crypto.generators.SCrypt;
import salt.hoprxi.crypto.HashService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/***
 * @author <a href="www.hoprxi.com/author/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.1 2019-04-15
 */
public class ScryptHash implements HashService {
    //CPU消耗参数，必须大于1, 是2的n次幂并且小于2^(128 * r / 8)
    private static final int CPU_COST = 65536;
    //the block size, must be >= 1.,块容量参数
    private static final int MEMORY_COST = 8;
    //并行化参数，一个小于等于((2^32-1) * hLen) / MFLen的正整数，其中hLen为 32，MFlen是128 * r。（在ltc协议中 p = 1）
    private static final int PARALLELISM = 1;
    //SALT LENGTH
    private static final int DK_LENGTH = 32;

    private static final int SALT_LENGTH = 16;

    @Override
    public String hash(String plainText) {
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        //SCryptPasswordEncoder encoder=new SCryptPasswordEncoder();
        //$s0$100801$VB1Bee8PS1Z3uSrd2P0N3w==$NvUE1B8nf4mpkjgWObAOzv1D0yZAKbjE829qZI6cLvU=
        byte[] encoded = SCrypt.generate(plainText.getBytes(StandardCharsets.UTF_8), salt, CPU_COST, MEMORY_COST, PARALLELISM, DK_LENGTH);
        String params = Long.toString(log2(CPU_COST) << 16L | MEMORY_COST << 8 | PARALLELISM, 16);
        StringBuilder sb = new StringBuilder((SALT_LENGTH + encoded.length) * 2);
        sb.append("$s0$").append(params).append('$')
                .append(Base64.getEncoder().encodeToString(salt)).append('$')
                .append(Base64.getEncoder().encodeToString(encoded));
        return sb.toString();
    }

    @Override
    public boolean matches(String plainText, String securedPlainTextHash) {
        if (securedPlainTextHash == null || securedPlainTextHash.length() < DK_LENGTH)
            return false;
        String[] parts = securedPlainTextHash.split("\\$");
        if (parts.length != 5 || !parts[1].equals("s0"))
            return false;

        long params = Long.parseLong(parts[2], 16);
        byte[] salt = Base64.getDecoder().decode(parts[3]);
        byte[] encoded = Base64.getDecoder().decode(parts[4]);

        int cpuCost = (int) Math.pow(2.0, (double) (params >> 16 & 65535L));
        int memoryCost = (int) params >> 8 & 255;
        int parallelization = (int) params & 255;

        byte[] generated = SCrypt.generate(plainText.getBytes(StandardCharsets.UTF_8), salt, cpuCost, memoryCost, parallelization, DK_LENGTH);

        return MessageDigest.isEqual(encoded, generated);
    }


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
}
