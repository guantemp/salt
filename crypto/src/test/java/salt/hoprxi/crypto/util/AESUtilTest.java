/*
 * Copyright (c) 2025. www.hoprxi.com All Rights Reserved.
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

package salt.hoprxi.crypto.util;

import org.testng.annotations.Test;
import salt.hoprxi.to.ByteToHex;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2024-05-29
 */
public class AESUtilTest {

    private static final String plainText = "Qwe123465Dw中文";
    private static SecretKey key;

    static {
        KeyGenerator kg1;
        try {
            kg1 = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        kg1.init(256, new SecureRandom("Qwe13465".getBytes(StandardCharsets.UTF_8)));//固定密码
        key = kg1.generateKey();
        System.out.println(Base64.getEncoder().encodeToString(key.getEncoded()));
    }

    @Test
    public void test() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String seed = "Qwe13465";

        // 第一次生成
        KeyGenerator keyGenerator1 = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom1 = new SecureRandom(seed.getBytes(StandardCharsets.UTF_8));
        keyGenerator1.init(256, secureRandom1);
        SecretKey secretKey1 = keyGenerator1.generateKey();
        String base64Key1 = Base64.getEncoder().encodeToString(secretKey1.getEncoded());
        System.out.println("Key 1: " + base64Key1);

        // 第二次生成
        KeyGenerator keyGenerator2 = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom2 = new SecureRandom(seed.getBytes(StandardCharsets.UTF_8));
        keyGenerator2.init(256, secureRandom2);
        SecretKey secretKey2 = keyGenerator2.generateKey();
        String base64Key2 = Base64.getEncoder().encodeToString(secretKey2.getEncoded());
        System.out.println("Key 2: " + base64Key2);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");

        PBEKeySpec spec = new PBEKeySpec(
                seed.toCharArray(),
                "Qwe123465".getBytes(StandardCharsets.UTF_8),
                256000,
                256
        );

        SecretKeySpec sks=new SecretKeySpec(
                factory.generateSecret(spec).getEncoded(),
                "AES"
        );
        System.out.println(Base64.getEncoder().encodeToString(sks.getEncoded()));
    }


    @Test
    public void testEncrypt() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] sources = plainText.getBytes(StandardCharsets.UTF_8);
        System.out.println("加密原文(text:base64):" + plainText + ":" + Base64.getEncoder().encodeToString(sources));

        byte[] encrypt = AESUtil.encrypt(sources, key);
        System.out.println("AES加密结果(hex):" + ByteToHex.toHexStr(encrypt));
        System.out.println("AES加密结果(base64):" + Base64.getEncoder().encodeToString(encrypt));
    }

    @Test
    public void testDecrypt() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] decrypt = AESUtil.decrypt(Base64.getDecoder().decode("/sITdvnejY/puuOIVPdVaILmbztcp46j6M73HcQQPyK9T2qxUEZ6XjIIHkBwUTP3"), key);
        System.out.println("AES解密结果(base64):" + new String(decrypt, StandardCharsets.UTF_8));

        decrypt = AESUtil.decrypt(ByteToHex.toBytes("e6632ab206c6f13f874058f6291f84154c5ca5fe0a28eb23097dc5fc000a9c596f9a4d3c25e5ce6567c7a05281e4239f"), key);
        System.out.println("AES解密结果(base64):" + new String(decrypt, StandardCharsets.UTF_8));
    }
}