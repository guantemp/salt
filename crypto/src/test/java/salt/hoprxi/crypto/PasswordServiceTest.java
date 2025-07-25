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

package salt.hoprxi.crypto;

import org.junit.Assert;
import org.testng.annotations.Test;
import salt.hoprxi.crypto.util.AESUtil;
import salt.hoprxi.to.ByteToHex;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Base64;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.3 builder 2024-09-07
 */
public class PasswordServiceTest {
    @Test(priority = 1)
    public void testMain() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnrecoverableKeyException {
        PasswordService.main(new String[]{"--help"});
        PasswordService.main(new String[]{"-S", "Qwe123465Gj"});
        PasswordService.main(new String[]{"-S", "postgresql.security.keystore.aes.password", "Qwe123465Pg"});
        PasswordService.main(new String[]{"-S", "elasticsearch.security.keystore.aes.password", "Qwe123465Pg", "Qwe123465"});
        PasswordService.main(new String[]{"-l"});
        PasswordService.main(new String[]{"-d","-l"});
        System.out.println("\n");

        PasswordService.main(new String[]{"-S", "Qwe123465Gj", "-f", "d:\\keystore.jks", "Qwe123465"});
        PasswordService.main(new String[]{"-S", "slave.tooo.top:6379", PasswordService.nextStrongPasswd(), "Qwe123465Re", "-f", "d:\\keystore.jks", "Qwe123465"});
        PasswordService.main(new String[]{"-S", "slave.tooo.top:9200", PasswordService.nextStrongPasswd(), "-f", "d:\\keystore.jks", "Qwe123465"});
        PasswordService.main(new String[]{"-S", "slave.tooo.top:6543", PasswordService.nextStrongPasswd(), "Qwe123465Pg", "-f", "d:\\keystore.jks", "Qwe123465"});
        System.out.println("\n");
        PasswordService.main(new String[]{"-l", "-f", "d:\\keystore.jks", "Qwe123465"});
        System.out.println("\n");
        //PasswordService.main(new String[]{"-e", "阿达沙发上"});
        //PasswordService.main(new String[]{"-e", "阿达沙发上", PasswordService.nextStrongPasswd()});
        //PasswordService.main(new String[]{"-e", "postgres", "129.28.29.105:5432", "Qwe123465Pg", "-f", "f:\\keystore.jks", "Qwe123465"});
        //PasswordService.main(new String[]{"-e", "Qwe123465", "129.28.29.105:5432", "Qwe123465Pg", "-f", "f:\\keystore.jks", "Qwe123465"});
        //PasswordService.main(new String[]{"-e", "admin", "120.77.47.145:6379", "Qwe123465Re", "-f", "f:\\keystore.jks", "Qwe123465"});
        PasswordService.main(new String[]{"-e", "Qwe123465", "slave.tooo.top:6379", "Qwe123465Re", "-f", "d:\\keystore.jks", "Qwe123465"});
        PasswordService.main(new String[]{"-e", "postgres", "slave.tooo.top:6543", "Qwe123465Pg", "-f", "d:\\keystore.jks", "Qwe123465"});
        PasswordService.main(new String[]{"-e", "Qwe123465", "slave.tooo.top:6543", "Qwe123465Pg", "-f", "d:\\keystore.jks", "Qwe123465"});
        PasswordService.main(new String[]{"-e", "elastic", "slave.tooo.top:9200", "-f", "d:\\keystore.jks", "Qwe123465"});
        PasswordService.main(new String[]{"-e", "Qwe123465", "slave.tooo.top:9200", "-f", "d:\\keystore.jks", "Qwe123465"});
    }

    @Test
    public void testNextPassword() {
        Assert.assertTrue(PasswordService.isStrong(PasswordService.nextPasswd()));
        Assert.assertTrue(PasswordService.isVeryStrong(PasswordService.nextStrongPasswd()));
        Assert.assertFalse(PasswordService.isVeryStrong(PasswordService.nextPasswd()));
        System.out.println("随机密码："+PasswordService.nextPasswd());
        System.out.println("加强随机密码："+PasswordService.nextStrongPasswd());
    }

    @Test
    public void testIsStrong() {
        Assert.assertTrue(PasswordService.isStrong("32534sdgd12"));
        Assert.assertFalse(PasswordService.isStrong("321569875"));
        Assert.assertTrue(PasswordService.isVeryStrong("#fcsd2yd54nb65"));
        Assert.assertFalse(PasswordService.isVeryStrong("32534sdgd12"));
    }

    @org.testng.annotations.Test
    public void testAes() throws NoSuchAlgorithmException, IOException, KeyStoreException, CertificateException, UnrecoverableKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");//SecureRandom.getInstance("SHA1PRNG");
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        System.out.println("AES随机初始化向量(BASE64):" + Base64.getEncoder().encodeToString(ivParameterSpec.getIV()));
        // 将KeyStore保存到文件
        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(null, null);

        KeyGenerator gen = KeyGenerator.getInstance("AES");
        gen.init(256, secureRandom);

        SecretKey secretKey = gen.generateKey();
        keyStore.setEntry("elasticsearch.security.keystore.password", new KeyStore.SecretKeyEntry(secretKey), new KeyStore.PasswordProtection("123".toCharArray()));
        System.out.println("AES随机密钥(elasticsearch):" + Base64.getEncoder().encodeToString(secretKey.getEncoded()));
        secretKey = gen.generateKey();
        System.out.println("AES随机密钥(postgresql):" + Base64.getEncoder().encodeToString(secretKey.getEncoded()));
        keyStore.setEntry("postgresql.security.keystore.password", new KeyStore.SecretKeyEntry(secretKey), new KeyStore.PasswordProtection("Qwe123465".toCharArray()));

        gen = KeyGenerator.getInstance("AES");
        gen.init(256, new SecureRandom("Qwe123465".getBytes(StandardCharsets.UTF_8)));
        SecretKey customizedKey = gen.generateKey();
        System.out.println("AES指定密钥:" + Base64.getEncoder().encodeToString(customizedKey.getEncoded()));
        keyStore.setEntry("customized.security.keystore.password", new KeyStore.SecretKeyEntry(customizedKey), new KeyStore.PasswordProtection("Qwe123465".toCharArray()));
        /*
        SecretKeyFactory factory=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        KeySpec spec=new PBEKeySpec("Qwe123465".toCharArray(),iv,32768,128);
        SecretKeySpec sKeySpec = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        System.out.println("AES定制密钥:" + Base64.toBase64String(sKeySpec.getEncoded()));
*/
        FileOutputStream fos = new FileOutputStream("keystore.jks");
        keyStore.store(fos, "".toCharArray());
        fos.close();

        byte[] sources = "Qwe123465德w".getBytes(StandardCharsets.UTF_8);
        byte[] aesData = AESUtil.encrypt(sources, customizedKey);

//        SecretKeySpec sKeySpec = new SecretKeySpec("Qwe123465".getBytes(StandardCharsets.UTF_8), "AES");
//        System.out.println("AES加密结果(byte)：");
//        for (byte b : aesData)
//            System.out.println(b);
        System.out.println("原始文本：Qwe123465德w");
        System.out.println("AES加密(base64)：" + Base64.getEncoder().encodeToString(aesData));
        System.out.println("AES加密(hex):" + ByteToHex.toHexStr(aesData));

        // 将KeyStore保存的密码取出来
        FileInputStream fis = new FileInputStream("keystore.jks");
        keyStore.load(fis, "".toCharArray());
        fis.close();

        SecretKey secKey = (SecretKey) keyStore.getKey("customized.security.keystore.password", "Qwe123465".toCharArray());
        keyStore.deleteEntry("");
        //System.out.println(Base64.getEncoder().encodeToString(secKey.getEncoded()));
        secureRandom.nextBytes(iv);
        byte[] decryptData = AESUtil.decrypt(aesData, secKey);
        System.out.println("AES解密：" + new String(decryptData, StandardCharsets.UTF_8));
    }
}