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

package salt.hoprxi.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.util.Arrays;
import java.util.Objects;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2024-05-29
 */
public class SM4Util {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String ALGORITHM_NAME_CBC_PADDING = "SM4/CBC/PKCS5Padding";

    /**
     * @param data
     * @param key
     * @return
     */

    public static byte[] encryptSpec(byte[] data, SecretKey key) {
        Objects.requireNonNull(data, "data is required");
        Objects.requireNonNull(key, "secretKey is required");
        SecureRandom secureRandom = new SecureRandom();//SecureRandom.getInstance("SHA1PRNG");
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        secureRandom.nextBytes(iv);
        byte[] mix = new byte[iv.length + data.length];
        System.arraycopy(iv, 0, mix, 0, 16);
        System.arraycopy(data, 0, mix, 16, data.length);
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_NAME_CBC_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
            return cipher.doFinal(mix);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Encrypt data[" + Arrays.toString(data) + "] exception", e);
        }
    }

    /**
     * @param data
     * @param key
     * @return
     */
    public static byte[] decryptSpec(byte[] data, SecretKey key) {
        Objects.requireNonNull(data, "data is required");
        Objects.requireNonNull(key, "secretKey is required");
        SecureRandom secureRandom = new SecureRandom();//SecureRandom.getInstance("SHA1PRNG");
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_NAME_CBC_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
            byte[] aesData = cipher.doFinal(data);
            byte[] result = new byte[aesData.length - 16];
            System.arraycopy(aesData, 16, result, 0, result.length);
            return result;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Encrypt data[" + Arrays.toString(data) + "] exception", e);
        }
    }
}
