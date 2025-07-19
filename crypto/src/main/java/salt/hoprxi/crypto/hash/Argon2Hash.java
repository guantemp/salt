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


import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import salt.hoprxi.crypto.HashService;

import java.security.SecureRandom;
import java.util.Base64;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2024-05-20
 */
public class Argon2Hash implements HashService {
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;// 输出密钥长度 (256位)
    private static final int PARALLELISM = 2;// 并行线程数
    private static final int MEMORY_COST = 64 * 1024;// 内存使用 (64 MB - 生产环境建议 > 64MB)
    private static final int ITERATIONS = 10;// 迭代次数 (推荐 > 10)

    /**
     * @param plainText
     * @return
     */
    @Override
    public String hash(String plainText) {
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        byte[] hash = new byte[HASH_LENGTH];

        Argon2Parameters params = (new Argon2Parameters.Builder(2)).withSalt(salt).withParallelism(PARALLELISM).withMemoryAsKB(MEMORY_COST).withIterations(ITERATIONS).build();
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);
        generator.generateBytes(plainText.toCharArray(), hash);
        return encode(hash, params);
    }

    private static String encode(byte[] hash, Argon2Parameters parameters) throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        String type;
        switch (parameters.getType()) {
            case 0:
                type = "$argon2d";
                break;
            case 1:
                type = "$argon2i";
                break;
            case 2:
                type = "$argon2id";
                break;
            default:
                throw new IllegalArgumentException("Invalid algorithm type: " + parameters.getType());
        }
        sb.append(type);
        sb.append("$v=").append(parameters.getVersion()).append("$m=").append(parameters.getMemory()).append(",t=").append(parameters.getIterations()).append(",p=").append(parameters.getLanes());
        if (parameters.getSalt() != null) {
            sb.append("$").append(Base64.getEncoder().encodeToString(parameters.getSalt()));
        }
        sb.append("$").append(Base64.getEncoder().encodeToString(hash));
        return sb.toString();
    }

    /**
     * @param plainText
     * @param securedPlainTextHash
     * @return
     */
    @Override
    public boolean matches(String plainText, String securedPlainTextHash) {
        if (securedPlainTextHash == null || securedPlainTextHash.length() < HASH_LENGTH)
            return false;
        String[] parts = securedPlainTextHash.split("\\$");
        if (parts.length != 6 || !parts[1].equals("argon2id"))
            throw new IllegalArgumentException("Invalid encoded Argon2-hash");

        Argon2Parameters.Builder builder;
        switch (parts[1]) {
            case "argon2d":
                builder = new Argon2Parameters.Builder(0);
                break;
            case "argon2i":
                builder = new Argon2Parameters.Builder(1);
                break;
            case "argon2id":
                builder = new Argon2Parameters.Builder(2);
                break;
            default:
                throw new IllegalArgumentException("Invalid algorithm type: " + parts[0]);
        }

        if (parts[2].startsWith("v=")) {
            builder.withVersion(Integer.parseInt(parts[2].substring(2)));
        }

        String[] performanceParams = parts[3].split(",");
        if (performanceParams.length != 3) {
            throw new IllegalArgumentException("Amount of performance parameters invalid");
        } else if (!performanceParams[0].startsWith("m=")) {
            throw new IllegalArgumentException("Invalid memory parameter");
        } else {
            builder.withMemoryAsKB(Integer.parseInt(performanceParams[0].substring(2)));
            if (!performanceParams[1].startsWith("t=")) {
                throw new IllegalArgumentException("Invalid iterations parameter");
            } else {
                builder.withIterations(Integer.parseInt(performanceParams[1].substring(2)));
                if (!performanceParams[2].startsWith("p=")) {
                    throw new IllegalArgumentException("Invalid parallelity parameter");
                } else {
                    builder.withParallelism(Integer.parseInt(performanceParams[2].substring(2)));
                    builder.withSalt(Base64.getDecoder().decode(parts[4]));//4 salt value
                }
            }
        }
        byte[] hash = new byte[HASH_LENGTH];
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(builder.build());
        generator.generateBytes(plainText.toCharArray(), hash);
//        for (String s : parts)
//            System.out.print("\"" + s + "\"\t");
        return constantTimeArrayEquals(hash, Base64.getDecoder().decode(parts[5]));//5 hash value
    }

    private static boolean constantTimeArrayEquals(byte[] expected, byte[] actual) {
        int diff = expected.length ^ actual.length;
        for (int i = 0; i < expected.length && i < actual.length; i++) {
            diff |= expected[i] ^ actual[i];
        }
        return diff == 0;
    }
}
