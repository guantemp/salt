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

package salt.hoprxi.crypto;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Assert;
import org.testng.annotations.Test;
import salt.hoprxi.to.ByteToHex;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2021-08-09
 */
public class PasswordServiceTest {
    @Test(priority = 1)
    public void testMain() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        PasswordService.main(new String[]{"-e", "阿达沙发上", "Qwe123465"});
    }

    @Test
    public void testGeneratePassword() {
        Assert.assertTrue(PasswordService.isStrong(PasswordService.generatePassword()));
        Assert.assertTrue(PasswordService.isVeryStrong(PasswordService.generateStrongPassword()));
        Assert.assertFalse(PasswordService.isVeryStrong(PasswordService.generatePassword()));
        System.out.println(PasswordService.generatePassword());
        System.out.println(PasswordService.generateStrongPassword());
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
        System.out.println("AES随机初始化向量(BASE64):" + Base64.toBase64String(ivParameterSpec.getIV()));
        // 将KeyStore保存到文件
        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(null, null);

        KeyGenerator gen = KeyGenerator.getInstance("AES");
        gen.init(256, secureRandom);

        SecretKey secretKey = gen.generateKey();
        keyStore.setEntry("elasticsearch.security.keystore.password", new KeyStore.SecretKeyEntry(secretKey), new KeyStore.PasswordProtection("123".toCharArray()));
        System.out.println("AES随机密钥(elasticsearch):" + Base64.toBase64String(secretKey.getEncoded()));
        secretKey = gen.generateKey();
        System.out.println("AES随机密钥(postgresql):" + Base64.toBase64String(secretKey.getEncoded()));
        keyStore.setEntry("postgresql.security.keystore.password", new KeyStore.SecretKeyEntry(secretKey), new KeyStore.PasswordProtection("Qwe123465".toCharArray()));

        gen = KeyGenerator.getInstance("AES");
        gen.init(256, new SecureRandom("Qwe123465".getBytes(StandardCharsets.UTF_8)));
        SecretKey customizedKey = gen.generateKey();
        System.out.println("AES指定密钥:" + Base64.toBase64String(customizedKey.getEncoded()));
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
        byte[] aesData = AesUtil.encryptSpec(sources, secretKey);

//        SecretKeySpec sKeySpec = new SecretKeySpec("Qwe123465".getBytes(StandardCharsets.UTF_8), "AES");
//        System.out.println("AES加密结果(byte)：");
//        for (byte b : aesData)
//            System.out.println(b);
        System.out.println("AES加密结果(base64)：" + Base64.toBase64String(aesData));
        System.out.println("AES加密结果(hex):" + ByteToHex.toHexStr(aesData));

        // 将KeyStore保存的密码取出来
        FileInputStream fis = new FileInputStream("keystore.jks");
        keyStore.load(fis, "".toCharArray());
        fis.close();

        SecretKey secKey = (SecretKey) keyStore.getKey("postgresql.security.keystore.password", "Qwe123465".toCharArray());
        secureRandom.nextBytes(iv);
        byte[] decryptData = AesUtil.decryptSpec(aesData, secKey);
        System.out.println("AES解密结果：" + new String(decryptData, StandardCharsets.UTF_8));
    }

    @org.testng.annotations.Test
    public void testKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair keyPair = generator.generateKeyPair();
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        System.out.println("RSA私钥：\n" + Base64.toBase64String(privateKey.getEncoded()));
        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
        System.out.println("RSA公钥：\n" + Base64.toBase64String(publicKey.getEncoded()));

        System.out.println("私钥加密:");
        byte[] encodedData = PasswordService.encrypt("寒山阿萨法千万人情味去我姥姥爱上合适的第三方挖了文人士大夫为了网天然了问题了合适的发生大幅了剃光头了过了23人我还是封杀哥哥老大哥电话3晚了点共和国饿他跟3 二哥和老公扔了涉案特了啊头； 为爱跳舞艾尔文特委屈我去儿童委屈月初℃".getBytes(StandardCharsets.UTF_8), privateKey);
        System.out.println(Base64.toBase64String(encodedData));
        System.out.println("公钥解密:");
        byte[] decodeData = PasswordService.decrypt(encodedData, publicKey);
        System.out.println(new String(decodeData, StandardCharsets.UTF_8));

        encodedData = PasswordService.encrypt("寒山阿萨法千万人情味去我姥姥爱上合适的第三方挖了文人士大夫为了网天然了问题了合适的发生大幅了剃光头了过了23人我还是封杀哥哥老大哥电话3晚了点共和国饿他跟3 二哥和老公扔了涉案特了啊头； 为爱跳舞艾尔文特委屈我去儿童委屈月初℃".getBytes(StandardCharsets.UTF_8), publicKey);
        System.out.println("公钥加密:\n" + Base64.toBase64String(encodedData));
        decodeData = PasswordService.decrypt(encodedData, privateKey);
        System.out.println("私钥解密\n" + new String(decodeData, StandardCharsets.UTF_8));
    }

    @org.testng.annotations.Test
    public void testStored() throws Exception {
        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(new ASN1InputStream(PasswordService.generatePublicKey(2048).getEncoded()).readObject());
        X500Name issueDn = new X500Name("C=CN,ST=SiChuan,L=LeShan,O=Skybility,OU=Cloudbility,CN=Atlas Personal License CA");
        X500Name subjectDn = new X500Name("C=CN,ST=SiChuan,L=LeShan,O=Skybility,OU=Cloudbility,CN=Atlas Personal License CA");
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Instant instant = Instant.now();
        Date notBefore = Date.from(instant);
        Date notAfter = Date.from(instant.plusSeconds((long) 30 * 24 * 3600));
        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(issueDn, serial, notBefore, notAfter, Locale.CHINA, subjectDn, subjectPublicKeyInfo);
        //证书签名数据
        ContentSigner signGen = new JcaContentSignerBuilder("SHA256withRSA").build(PasswordService.generatePrivateKey(2048));
        X509CertificateHolder holder = builder.build(signGen);
        byte[] certBuf = holder.getEncoded();
        X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("x509").generateCertificate(new ByteArrayInputStream(certBuf));

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry("hoprxi", PasswordService.generatePrivateKey(2048), "123456".toCharArray(), new X509Certificate[]{certificate});
        keyStore.store(new FileOutputStream("hoprxi.p12"), "123456789".toCharArray());
    }

    @Test
    public void testSign() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair keyPair = generator.generateKeyPair();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);

        byte[] sign = PasswordService.sign("我说我是早上好aSD暗示反帝反封的时候工地上第三方的第三个和我聊的时候工地上第三个回答说儿童".getBytes(), privateKey);
        System.out.println("签名:\n" + Base64.toBase64String(sign));
        Assert.assertTrue(PasswordService.verify("我说我是早上好aSD暗示反帝反封的时候工地上第三方的第三个和我聊的时候工地上第三个回答说儿童".getBytes(), sign, publicKey));
    }
}