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

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.crypto.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2024-05-30
 */
public class EncryptService {

    public static Key[] nextRSAKeyPair(int keySize) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(keySize, SecureRandom.getInstanceStrong());
        KeyPair keyPair = generator.generateKeyPair();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
        return new Key[]{keyFactory.generatePublic(x509KeySpec), keyFactory.generatePrivate(pkcs8KeySpec)};
    }

    public static void createCertificate() {

    }

    /**
     * @param data
     * @param privateKey
     * @return
     */
    public static byte[] encrypt(byte[] data, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            int keySize = ((RSAPrivateKey) privateKey).getModulus().bitLength();
            return crypt(cipher, Cipher.ENCRYPT_MODE, data, keySize);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | ShortBufferException |
                 IllegalBlockSizeException | BadPaddingException | IOException e) {
            throw new RuntimeException("Encrypt data[" + Arrays.toString(data) + "] exception", e);
        }
    }

    public static byte[] encrypt(byte[] data, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            int keySize = ((RSAPublicKey) publicKey).getModulus().bitLength();
            return crypt(cipher, Cipher.ENCRYPT_MODE, data, keySize);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | ShortBufferException |
                 IllegalBlockSizeException | BadPaddingException | IOException e) {
            throw new RuntimeException("Encrypt data[" + Arrays.toString(data) + "] exception", e);
        }
    }

    /**
     * @param data
     * @param privateKey
     * @return
     */
    public static byte[] decrypt(byte[] data, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            int keySize = ((RSAPrivateKey) privateKey).getModulus().bitLength();
            return crypt(cipher, Cipher.DECRYPT_MODE, data, keySize);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | ShortBufferException |
                 IllegalBlockSizeException | BadPaddingException | IOException e) {
            throw new RuntimeException("Decrypt data[" + Arrays.toString(data) + "] exception", e);
        }
    }

    /**
     * @param data
     * @param publicKey
     * @return
     */
    public static byte[] decrypt(byte[] data, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            int keySize = ((RSAPublicKey) publicKey).getModulus().bitLength();
            return crypt(cipher, Cipher.DECRYPT_MODE, data, keySize);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | ShortBufferException |
                 IllegalBlockSizeException | BadPaddingException | IOException e) {
            throw new RuntimeException("Decrypt data[" + Arrays.toString(data) + "] exception", e);
        }
    }


    /**
     * @param cipher
     * @param opmode
     * @param data
     * @param keySize
     * @return
     * @throws ShortBufferException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException
     */
    private static byte[] crypt(Cipher cipher, int opmode, byte[] data, int keySize) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException, IOException {
        int blockSize = (opmode == Cipher.DECRYPT_MODE) ? keySize / 8 : keySize / 8 - 11;
        int dataLength = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密解密
        while (dataLength - offset > 0) {
            if (dataLength - offset > blockSize) {
                cache = cipher.doFinal(data, offset, blockSize);
            } else {
                cache = cipher.doFinal(data, offset, dataLength - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * blockSize;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    /**
     * @param data
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static byte[] sign(byte[] data, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey key = keyFactory.generatePrivate(keySpec);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(key);
        signature.update(data);
        return signature.sign();
    }

    /**
     * @param src
     * @param sign
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static boolean verify(byte[] src, byte[] sign, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        byte[] keyBytes = publicKey.getEncoded();
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey key = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(key);
        signature.update(src);
        return signature.verify(sign);
    }

    private static Certificate generateV3(String issuer, String subject,
                                          BigInteger serial, Date notBefore, Date notAfter,
                                          PublicKey publicKey, PrivateKey privKey, List<Extension> extensions)
            throws OperatorCreationException, CertificateException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Key[] keys = EncryptService.nextRSAKeyPair(2048);
        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(new ASN1InputStream(keys[0].getEncoded()).readObject());
        X500Name issueDn = new X500Name("C=CN,ST=SiChuan,L=LeShan,O=Skybility,OU=Cloudbility,CN=Atlas Personal License CA");
        X500Name subjectDn = new X500Name("C=CN,ST=SiChuan,L=LeShan,O=Skybility,OU=Cloudbility,CN=Atlas Personal License CA");
        //Instant instant = Instant.now();
        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(issueDn, serial, notBefore, notAfter, Locale.CHINA, subjectDn, subjectPublicKeyInfo);
        //证书签名数据
        ContentSigner signGen = new JcaContentSignerBuilder("SHA256withRSA").build((PrivateKey) keys[1]);
        X509CertificateHolder holder = builder.build(signGen);
        byte[] certBuf = holder.getEncoded();
        X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("x509").generateCertificate(new ByteArrayInputStream(certBuf));
        return certificate;
    }
}
