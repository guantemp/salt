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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import salt.hoprxi.crypto.HashService;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/***
 * @author <a href="www.hoprxi.com/author/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.2 2022-08-10
 */
@Deprecated
public class Pbhkdf2Hash implements HashService {
    private static final Logger LOGGER = LoggerFactory.getLogger(Pbhkdf2Hash.class);
    private static final int HASH = 256;
    private static final int ITERATIONS = 16384;
    private static final String SECRET_NAME = "PBKDF2WithHmacSHA512";

    @Override
    public boolean matches(String plainText, String securedPlainTextHash) {
        String[] parts = securedPlainTextHash.split(":");
        byte[] salt = fromHex(parts[0]);
        byte[] hash = fromHex(parts[1]);
        char[] chars = plainText.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, ITERATIONS, HASH);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(SECRET_NAME);
            byte[] testHash = skf.generateSecret(spec).getEncoded();
            int diff = hash.length ^ testHash.length;
            for (int i = 0; i < hash.length && i < testHash.length; i++) {
                diff |= hash[i] ^ testHash[i];
            }
            return diff == 0;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Encrypt is fail", e);
        }
        return false;
    }

    @Override
    public String hash(String plainText) {
        char[] chars = plainText.toCharArray();
        try {
            byte[] salt = getSalt();
            PBEKeySpec spec = new PBEKeySpec(chars, salt, ITERATIONS, HASH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(SECRET_NAME);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return toHex(salt) + ":" + toHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Encrypt is fail", e);
        }
        return plainText;
    }

    /**
     * @param hex
     * @return
     */
    private byte[] fromHex(String hex) {
        int length = hex.length() / 2;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++)
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        return bytes;
    }

    /**
     * @return
     * @throws NoSuchAlgorithmException
     */
    private byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstanceStrong();
        byte[] salt = new byte[32];
        sr.nextBytes(salt);
        return salt;
    }

    /**
     * @param array
     * @return
     */
    private String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }
}
