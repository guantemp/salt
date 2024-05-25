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
package salt.hoprxi.crypto.application;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.regex.Pattern;

/***
 * @author <a href="www.hoprxi.com/author/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.4 2024-04-24
 */
public class PasswordService {
    private static final String DIGITS = "0123456789";
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String SYMBOLS = "\"`!?$?%^&*()_-+={[}]:;@'~#|\\<,>.?/";
    private static final int STRONG_THRESHOLD = 20;
    private static final int VERY_STRONG_THRESHOLD = 40;
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\u4e00-\u9fa5]");
    private static final Pattern EXCLUDE = Pattern.compile("^-{1,}.*");

    public static void main(String[] args) throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException {
        String param1 = null, param2 = null, param3 = null, param4 = null;
        String fileName = "keystore.jks";
        boolean encryptSign = false, fileSign = false, storeSign = false, helpSign = false;
        for (int i = 0, j = args.length; i < j; i++) {
            switch (args[i]) {
                case "-f":
                case "--file":
                    fileSign = true;
                    if (j > i + 1 && !EXCLUDE.matcher(args[i + 1]).matches()) {
                        fileName = args[i + 1];
                    }
                    break;
                case "-e":
                    encryptSign = true;
                    if (j > i + 1) {
                        if (EXCLUDE.matcher(args[i + 1]).matches())
                            break;
                        else
                            param1 = args[i + 1];
                    }
                    if (j > i + 2) {
                        if (EXCLUDE.matcher(args[i + 2]).matches())
                            break;
                        else
                            param2 = args[i + 2];
                    }
                    if (j > i + 3) {
                        if (EXCLUDE.matcher(args[i + 3]).matches())
                            break;
                        else
                            param3 = args[i + 3];
                    }
                    if (j > i + 4) {
                        if (EXCLUDE.matcher(args[i + 4]).matches())
                            break;
                        else
                            param4 = args[i + 4];
                    }
                    break;
                case "-S":
                    storeSign = true;
                    if (j > i + 1) {
                        if (EXCLUDE.matcher(args[i + 1]).matches())
                            break;
                        else
                            param1 = args[i + 1];
                    }
                    if (j > i + 2) {
                        if (EXCLUDE.matcher(args[i + 2]).matches())
                            break;
                        else
                            param2 = args[i + 2];
                    }
                    if (j > i + 3) {
                        if (EXCLUDE.matcher(args[i + 3]).matches())
                            break;
                        else
                            param3 = args[i + 3];
                    }
                    break;
                case "-h":
                case "--help":
                    helpSign = true;
                    break;
            }
        }
        if (helpSign || args.length == 0) {
            System.out.println("Non-option arguments:\n" +
                    "command              \n" +
                    "\n" +
                    "Option                         Description        \n" +
                    "------                         -----------        \n" +
                    "-S <KeyValuePair>              configure a setting\n" +
                    "-e <KeyValuePair>              encrypt a passwd\n" +
                    "-t --type                      encrypt type(aes,sm4)\n" +
                    "-l, --list                     entries in the keystore\n" +
                    "-h, --help                     Show help          \n" +
                    "-p, --passwd                   Encrypt password\n" +
                    "-pp,--PasswordProtection       Encrypt password protection\n" +
                    "-f, --file                     Show verbose output\n");
        } else {
            if (encryptSign) {
                if (fileSign) {
                    encryptWithStorePasswd(param1, param2, param3, fileName, param4);
                } else {
                    encrypt(param1, param2);
                }
            }
            if (storeSign) {
                store(param1, param2, param3, fileName);
            }
        }
    }

    private static void encryptWithStorePasswd(String planText, String entry, String entryPasswd, String fileName, String protectedPasswd) throws NoSuchAlgorithmException, CertificateException, KeyStoreException {
        Objects.requireNonNull(planText, "planText required");
        //if(entry)
        try (FileInputStream fis = new FileInputStream(fileName)) {
            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            keyStore.load(fis, protectedPasswd.toCharArray());
            //if (entryPasswd == null) entryPasswd = filePasswd;
            SecretKey secKey = (SecretKey) keyStore.getKey(entry, entryPasswd.toCharArray());
            encrypt(planText, secKey, protectedPasswd);
        } catch (FileNotFoundException e) {
            System.out.println("Not find key store file：" + fileName);
        } catch (IOException e) {
            System.out.println("Keystore password was incorrect" + protectedPasswd);
        } catch (UnrecoverableKeyException e) {
            System.out.println("Is a bad key is used during decryption" + protectedPasswd);
        }
    }

    private static void encrypt(String planText, String password) throws NoSuchAlgorithmException {
        Objects.requireNonNull(planText, "planText required");
        if (password == null)
            password = PasswordService.generatePassword();
        KeyGenerator gen = KeyGenerator.getInstance("AES");
        gen.init(256, new SecureRandom(password.getBytes(StandardCharsets.UTF_8)));
        SecretKey secretKey = gen.generateKey();
        encrypt(planText, secretKey, password);
    }

    private static void encrypt(String planText, SecretKey secretKey, String password) {
        SecureRandom secureRandom = new SecureRandom();//SecureRandom.getInstance("SHA1PRNG");
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        byte[] sources = planText.getBytes(StandardCharsets.UTF_8);
        byte[] mix = new byte[iv.length + sources.length];
        secureRandom.nextBytes(iv);
        System.arraycopy(iv, 0, mix, 0, 16);
        System.arraycopy(sources, 0, mix, 16, sources.length);

        byte[] aesData = PasswordService.encrypt(mix, secretKey, ivParameterSpec);

        System.out.println("The plan text encrypted\n" +
                "------                -----------        \n" +
                "plan_text                " + planText + "\n" +
                "password                 " + password + "\n" +
                "encrypted(base64)        " + Base64.toBase64String(aesData)
        );
    }


    private static void store(String param1, String param2, String param3, String filename) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        String protectPasswd = "", entry = "security.keystore.password", entryPasswd = PasswordService.generateStrongPassword();
        if (param1 != null && param2 != null && param3 != null) {
            protectPasswd = param1;
            entry = param2;
            entryPasswd = param3;
        } else if (param1 != null && param2 != null) {
            entry = param1;
            entryPasswd = param2;
        } else if (param1 != null) {
            entryPasswd = param1;
        }
        System.out.println(param1 + ":" + param2 + ":" + param3);
        System.out.println(protectPasswd + ":" + entry + ":" + entryPasswd);
        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(null, null);
        File file = new File(filename);
        if (file.exists()) {
            keyStore.load(Files.newInputStream(file.toPath()), protectPasswd.toCharArray());
        }

        KeyGenerator gen = KeyGenerator.getInstance("AES");
        gen.init(256, new SecureRandom(entryPasswd.getBytes(StandardCharsets.UTF_8)));
        SecretKey customizedKey = gen.generateKey();
        keyStore.setEntry(entry, new KeyStore.SecretKeyEntry(customizedKey), new KeyStore.PasswordProtection(entryPasswd.toCharArray()));

        FileOutputStream fos = new FileOutputStream(file);
        keyStore.store(Files.newOutputStream(file.toPath()), protectPasswd.toCharArray());
        fos.close();

        System.out.println("Password has been saved to ‘" + file.getAbsolutePath() + "’\n" +
                "--------------               ------------------        \n" +
                "Protect Password             " + protectPasswd + "\n" +
                "Entry                        " + entry + "\n" +
                "Entry Password               " + entryPasswd + "\n" +
                "Entry Protect Password       " + entryPasswd
        );
    }

    /**
     * @return
     */
    public static String generatePassword() {
        String password = null;
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        boolean isStrong = false;
        while (!isStrong) {
            password = generatePassword(sb, random);
            if (password.length() >= 8) {
                isStrong = isStrong(password);
            }
        }
        return password;
    }

    /**
     * @return
     */
    public static String generateStrongPassword() {
        String password = null;
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        boolean isStrong = false;
        while (!isStrong) {
            password = generatePassword(sb, random);
            if (password.length() >= 12) {
                isStrong = isVeryStrong(password);
            }
        }
        return password;
    }

    /**
     * @param password
     * @param random
     * @return
     */
    private static String generatePassword(StringBuilder password, SecureRandom random) {
        int index;
        int opt = random.nextInt(4);
        switch (opt) {
            case 0:
                index = random.nextInt(LETTERS.length());
                password.append(LETTERS, index, index + 1);
                break;
            case 1:
                index = random.nextInt(LETTERS.length());
                password.append(LETTERS.substring(index, index + 1).toLowerCase());
                break;
            case 2:
                index = random.nextInt(DIGITS.length());
                password.append(DIGITS, index, index + 1);
                break;
            case 3:
                index = random.nextInt(SYMBOLS.length());
                password.append(SYMBOLS, index, index + 1);
                break;
        }
        return password.toString();
    }

    /**
     * @param aPlainTextPassword
     * @return
     */
    public static boolean isStrong(String aPlainTextPassword) {
        return calculatePasswordStrength(aPlainTextPassword) >= STRONG_THRESHOLD;
    }

    /**
     * @param aPlainTextPassword
     * @return
     */
    public static boolean isVeryStrong(String aPlainTextPassword) {
        return calculatePasswordStrength(aPlainTextPassword) >= VERY_STRONG_THRESHOLD;
    }

    /**
     * @param plainTextPassword 文本密码
     * @return
     */
    private static int calculatePasswordStrength(String plainTextPassword) {
        plainTextPassword = Objects.requireNonNull(plainTextPassword, "Plain text password is required");
        if (CHINESE_PATTERN.matcher(plainTextPassword).matches())
            throw new IllegalArgumentException("Cannot contain Chinese characters");
        int strength = 0;
        int length = plainTextPassword.length();
        if (length > 6) {
            strength += 10;
            // bonus: one point each additional
            strength += (length - 6);
        }
        int digitCount = 0;
        int letterCount = 0;
        int lowerCount = 0;
        int upperCount = 0;
        int symbolCount = 0;
        for (int idx = 0; idx < length; ++idx) {
            char ch = plainTextPassword.charAt(idx);
            if (Character.isLetter(ch)) {
                ++letterCount;
                if (Character.isUpperCase(ch)) {
                    ++upperCount;
                } else {
                    ++lowerCount;
                }
            } else if (Character.isDigit(ch)) {
                ++digitCount;
            } else {
                ++symbolCount;
            }
        }
        strength += (upperCount + lowerCount + symbolCount);
        // bonus: letters and digits
        if (letterCount >= 2 && digitCount >= 2) {
            strength += (letterCount + digitCount);
        }
        return strength;
    }

    /**
     * @param data
     * @param secretKey
     * @param spec
     * @return
     */
    public static byte[] encrypt(byte[] data, SecretKey secretKey, IvParameterSpec spec) {
        Objects.requireNonNull(secretKey, "secretKey is required");
        Objects.requireNonNull(spec, "Iv Parameter Spec is required");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            return cipher.doFinal(data);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Encrypt data[" + Arrays.toString(data) + "] exception", e);
        }
    }

    /**
     * @param data
     * @param secretKey
     * @param spec
     * @return
     */
    public static byte[] decrypt(byte[] data, SecretKey secretKey, IvParameterSpec spec) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            return cipher.doFinal(data);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Encrypt data[" + Arrays.toString(data) + "] exception", e);
        }
    }

    /**
     * @param data
     * @param secretKey
     * @return
     */
    public static byte[] decryptRemoveIV(byte[] data, SecretKey secretKey) throws NoSuchAlgorithmException {
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();//SecureRandom.getInstance("SHA1PRNG");
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        byte[] aesData = decrypt(data, secretKey, ivParameterSpec);
        byte[] result = new byte[aesData.length - 16];
        System.arraycopy(aesData, 16, result, 0, result.length);
        return result;
    }

    public static PrivateKey generatePrivateKey(int keySize) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(keySize, new SecureRandom());
        KeyPair keyPair = generator.generateKeyPair();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());
        return keyFactory.generatePrivate(pkcs8KeySpec);
    }

    public static PublicKey generatePublicKey(int keySize) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(keySize, new SecureRandom());
        KeyPair keyPair = generator.generateKeyPair();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
        return keyFactory.generatePublic(x509KeySpec);
    }

    public static void generateRsaKeyPair(int keySize, OutputStream out) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(keySize, new SecureRandom());
        KeyPair keyPair = generator.generateKeyPair();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded());
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
        keyFactory.generatePrivate(pkcs8KeySpec);
        keyFactory.generatePublic(x509KeySpec);
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
        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(new ASN1InputStream(PasswordService.generatePublicKey(2048).getEncoded()).readObject());
        X500Name issueDn = new X500Name("C=CN,ST=SiChuan,L=LeShan,O=Skybility,OU=Cloudbility,CN=Atlas Personal License CA");
        X500Name subjectDn = new X500Name("C=CN,ST=SiChuan,L=LeShan,O=Skybility,OU=Cloudbility,CN=Atlas Personal License CA");
        //Instant instant = Instant.now();
        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(issueDn, serial, notBefore, notAfter, Locale.CHINA, subjectDn, subjectPublicKeyInfo);
        //证书签名数据
        ContentSigner signGen = new JcaContentSignerBuilder("SHA256withRSA").build(PasswordService.generatePrivateKey(2048));
        X509CertificateHolder holder = builder.build(signGen);
        byte[] certBuf = holder.getEncoded();
        X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("x509").generateCertificate(new ByteArrayInputStream(certBuf));
        return certificate;
    }
}
