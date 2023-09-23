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
import salt.hoprxi.crypto.application.PasswordService;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
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

    @Test
    public void testGeneratePassword() {
        Assert.assertTrue(PasswordService.isStrong(PasswordService.generateStrongPassword()));
        System.out.println(PasswordService.generateStrongPassword());
        System.out.println(PasswordService.generateVeryStrongPassword());
        System.out.println(PasswordService.generateStrongPassword());
        System.out.println(PasswordService.generateVeryStrongPassword());
    }

    @Test
    public void testIsStrong() {
        Assert.assertTrue(PasswordService.isStrong("32534sdgd12"));
        Assert.assertTrue(PasswordService.isVeryStrong("#fcsd2yd54nb65"));
    }

    @Test
    public void testAes() throws NoSuchAlgorithmException {
        KeyGenerator gen = KeyGenerator.getInstance("AES");
        gen.init(new SecureRandom());
        SecretKey secretKey = gen.generateKey();
        System.out.println("AES密钥:\n" + Base64.toBase64String(secretKey.getEncoded()));
        //SecretKeySpec sKeySpec = new SecretKeySpec(Base64.decode("CTG2uR3Skva+/06DoQQ4SV6QThGVHCKTsB0RE/FCwBU="), "AES");
        SecureRandom randomSecureRandom = new SecureRandom();//SecureRandom.getInstance("SHA1PRNG");
        byte[] iv = new byte[16];
        randomSecureRandom.nextBytes(iv);
        System.out.println("IV：\n" + Base64.toBase64String(iv));
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        byte[] aesData = PasswordService.encrypt("十多个和我亲吻了去三分大赛是大幅杀跌奥拉夫危34 第三个大厦还有了我3天然砂去了好多个电话2U影你可以阿瑟提前我萨尔图父亲而我忘了为了网络为了2 问了很多人说沙发啥的委屈儿童核武器".getBytes(StandardCharsets.UTF_8),
                secretKey, ivParameterSpec);
        System.out.println("AES加密：\n" + Base64.toBase64String(aesData));
        aesData = PasswordService.decrypt(aesData, secretKey, ivParameterSpec);
        System.out.println("AES解密：\n" + new String(aesData, StandardCharsets.UTF_8));
    }

    @Test
    public void testKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair keyPair = generator.generateKeyPair();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
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

    @Test
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