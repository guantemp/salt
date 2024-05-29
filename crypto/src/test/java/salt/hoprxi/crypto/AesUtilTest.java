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

import org.testng.annotations.Test;
import salt.hoprxi.to.ByteToHex;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Base64;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2024-05-29
 */
public class AesUtilTest {

    private static final String PASSWD="Qwe123465Dw";

    @Test
    public void testEncryptSpec() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256, new SecureRandom("Qwe13465".getBytes(StandardCharsets.UTF_8)));//固定密码
        SecretKey key = kg.generateKey();

        byte[] sources = PASSWD.getBytes(StandardCharsets.UTF_8);
        System.out.println("加密原文(text:base64):" +PASSWD+"$"+ Base64.getEncoder().encodeToString(sources));

        byte[] encrypt = AesUtil.encryptSpec(sources, key);
        System.out.println("SM4加密结果(hex):" + ByteToHex.toHexStr(encrypt));
        System.out.println("SM4加密结果(base64):" + Base64.getEncoder().encodeToString(encrypt));
    }

    @Test
    public void testDecryptSpec() throws NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256, new SecureRandom("Qwe13465".getBytes(StandardCharsets.UTF_8)));//固定密码
        SecretKey key = kg.generateKey();

        byte[] decrypt = AesUtil.decryptSpec(Base64.getDecoder().decode("Piksg/i3j5lIVY0FKba+Q3K+r8DH9Zj9kNMPFRZQ904="), key);
        System.out.println("SM4解密结果(base64):" + new String(decrypt, StandardCharsets.UTF_8));
        decrypt = AesUtil.decryptSpec(ByteToHex.toBytes("ab265b8d0a6aaf7c57761a5328b15350b380ed218009b698efc7821d492586461c725ea1af391e45597e44334b329174"), key);
        System.out.println("SM4解密结果(base64):" + new String(decrypt, StandardCharsets.UTF_8));
    }
}