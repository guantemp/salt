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
import org.testng.annotations.Test;
import salt.hoprxi.to.ByteToHex;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2024-05-29
 */
public class SM4UtilTest {

    private static final String PASSWD = "Qwe123465Dw中文";

    @Test
    public void testEncryptSpec() throws NoSuchAlgorithmException, NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());

        KeyGenerator kg = KeyGenerator.getInstance("SM4", BouncyCastleProvider.PROVIDER_NAME);
        kg.init(128, new SecureRandom("Qwe13465".getBytes(StandardCharsets.UTF_8)));//固定密码
        SecretKey key = kg.generateKey();

        byte[] sources = PASSWD.getBytes(StandardCharsets.UTF_8);
        System.out.println("加密原文(text:base64):" + PASSWD + "$" + Base64.getEncoder().encodeToString(sources));

        byte[] encrypt = SM4Util.encryptSpec(sources, key);
        System.out.println("SM4加密结果(hex):" + ByteToHex.toHexStr(encrypt));
        System.out.println("SM4加密结果(base64):" + Base64.getEncoder().encodeToString(encrypt));
    }

    @Test(priority = 1)
    public void testDecryptSpec() throws NoSuchAlgorithmException, NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());

        KeyGenerator kg = KeyGenerator.getInstance("SM4", BouncyCastleProvider.PROVIDER_NAME);
        kg.init(128, new SecureRandom("Qwe13465".getBytes(StandardCharsets.UTF_8)));//固定密码
        SecretKey key = kg.generateKey();

        byte[] decrypt = SM4Util.decryptSpec(Base64.getDecoder().decode("ovi/qagixOGaqnoC8OCA52QI9D/S9Q7dILkeMchfIs+oiSQtZWlDn1HK+ew6FS8l"), key);
        System.out.println("SM4解密结果(base64):" + new String(decrypt, StandardCharsets.UTF_8));
        decrypt = SM4Util.decryptSpec(ByteToHex.toBytes("14840b84ce48ecf4791ace0e4aff124c0e6283338c83770d9efafc38aee59a6c"), key);
        System.out.println("SM4解密结果(base64):" + new String(decrypt, StandardCharsets.UTF_8));
        //Assert.assertEquals("Qwe123465",new String(decrypt, StandardCharsets.UTF_8));
    }
}