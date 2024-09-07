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

import org.bouncycastle.util.encoders.Base64;
import salt.hoprxi.crypto.util.AESUtil;
import salt.hoprxi.to.ByteToHex;

import javax.crypto.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Objects;
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

    private enum ActionTag {
        DELETE, LIST, STORE, ENCRYPT, FILE, HELP
    }

    public static final String KEYSTORE_ENTRY = "security.keystore.aes.password";

    public static void main(String[] args) throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnrecoverableKeyException {
        String param1 = null, param2 = null, param3 = null;
        String fileName = "keystore.jks", protectPasswd = "";
        EnumSet<ActionTag> set = EnumSet.noneOf(ActionTag.class);
        for (int i = 0, j = args.length; i < j; i++) {
            switch (args[i]) {
                case "-f":
                case "--file":
                    set.add(ActionTag.FILE);
                    if (j > i + 1) {
                        if (EXCLUDE.matcher(args[i + 1]).matches())
                            break;
                        else
                            fileName = args[i + 1];
                    }
                    if (j > i + 2) {
                        if (EXCLUDE.matcher(args[i + 2]).matches())
                            break;
                        else
                            protectPasswd = args[i + 2];
                    }
                    break;
                case "-e":
                    set.add(ActionTag.ENCRYPT);
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
                case "-S":
                    set.add(ActionTag.STORE);
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
                case "-t":
                case "--type":
                    break;
                case "-l":
                case "--list":
                    set.add(ActionTag.LIST);
                    break;
                case "-d":
                case "--delete":
                    set.add(ActionTag.DELETE);
                    if (j > i + 1) {
                        if (EXCLUDE.matcher(args[i + 1]).matches())
                            break;
                        else
                            param1 = args[i + 1];
                    }
                    break;
                case "-h":
                case "--help":
                    set.add(ActionTag.HELP);
                    break;
            }
        }
        if (set.contains(ActionTag.HELP) || args.length == 0) {
            System.out.println("Non-option arguments:\n" +
                    "command              \n" +
                    "\n" +
                    "Option                         Description        \n" +
                    "------                         -----------        \n" +
                    "-S <KeyValuePair>              configure a setting\n" +
                    "-e <KeyValuePair>              encrypt a passwd\n" +
                    "-t --type                      encrypt type(aes,sm4)\n" +
                    "-d --del                       delete a entry\n" +
                    "-l, --list                     entries in the keystore\n" +
                    "-h, --help                     Show help          \n" +
                    "-f, --file                     Show verbose output\n");
        } else {
            if (set.contains(ActionTag.STORE)) {
                store(param1, param2, param3, fileName, protectPasswd);
            } else if (set.contains(ActionTag.DELETE)) {
                delete(param1, fileName, protectPasswd);
            } else if (set.contains(ActionTag.ENCRYPT)) {
                if (set.contains(ActionTag.FILE)) {
                    encryptWithStore(param1, param2, param3, fileName, protectPasswd);
                } else {
                    encrypt(KEYSTORE_ENTRY, param1, param2);
                }
            }
            if (set.contains(ActionTag.LIST))
                list(fileName, protectPasswd);
        }
    }

    private static void store(String param1, String param2, String param3, String filename, String protectPasswd) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        String entry = KEYSTORE_ENTRY, entryPasswd = PasswordService.nextStrongPasswd(), entryProtectPasswd = "";
        if (param1 != null && param2 != null && param3 != null) {
            entry = param1;
            entryPasswd = param2;
            entryProtectPasswd = param3;
        } else if (param1 != null && param2 != null) {
            entry = param1;
            entryPasswd = param2;
        } else if (param1 != null) {
            entryPasswd = param1;
        }
        //System.out.println(param1 + ":" + param2 + ":" + param3);
        //System.out.println(entry + ":" + entryPasswd + ":" + entryProtectPasswd);
        KeyGenerator gen = KeyGenerator.getInstance("AES");
        gen.init(256, new SecureRandom(entryPasswd.getBytes(StandardCharsets.UTF_8)));
        SecretKey customizedKey = gen.generateKey();

        KeyStore keyStore = KeyStore.getInstance("JCEKS");//JKS,JCEKS(推荐）,PKCS12(RSA存储为p12),BKS(bouncycastle),UBER
        keyStore.load(null, protectPasswd.toCharArray());
        File file = new File(filename);
        if (file.exists()) {
            keyStore.load(Files.newInputStream(file.toPath()), protectPasswd.toCharArray());
        }
        keyStore.setEntry(entry, new KeyStore.SecretKeyEntry(customizedKey), new KeyStore.PasswordProtection(entryProtectPasswd.toCharArray()));

        FileOutputStream fos = new FileOutputStream(file);
        keyStore.store(Files.newOutputStream(file.toPath()), protectPasswd.toCharArray());
        fos.close();

        System.out.println("Password has been saved to ‘" + file.getAbsolutePath() + "’\n" +
                "----------------------             -----------------------------\n" +
                "Entry                              " + entry + "\n" +
                "Entry Saved Password               " + entryPasswd + "\n" +
                "Entry Protect Password             " + (entryProtectPasswd.equalsIgnoreCase("") ? "<Empty>" : entryProtectPasswd) + "\n" +
                "File Protect Password              " + (protectPasswd.equalsIgnoreCase("") ? "<Empty>" : protectPasswd)
        );
    }

    private static void list(String fileName, String protectPasswd) throws KeyStoreException, CertificateException, NoSuchAlgorithmException {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            keyStore.load(fis, protectPasswd.toCharArray());
            Enumeration<String> alias = keyStore.aliases();
            System.out.println("List entry from: " + fileName);
            while (alias.hasMoreElements()) {
                System.out.println(alias.nextElement());
            }
        } catch (IOException e) {
            System.out.println("Keystore was not exists filename=：" + fileName);
        }
    }

    private static void delete(String entry, String fileName, String protectPasswd) throws KeyStoreException, CertificateException, NoSuchAlgorithmException {
        if (entry == null) {
            entry = KEYSTORE_ENTRY;
        }
        try (FileInputStream fis = new FileInputStream(fileName)) {
            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            keyStore.load(fis, protectPasswd.toCharArray());
            if (keyStore.containsAlias(entry)) {
                keyStore.deleteEntry(entry);
                FileOutputStream fos = new FileOutputStream(fileName);
                keyStore.store(fos, protectPasswd.toCharArray());
                fos.close();
                System.out.println("Entry deleted: " + entry);
            } else {
                System.out.println("Not find entry: " + entry);
            }
        } catch (IOException e) {
            System.out.println("Keystore was not exists filename=：" + fileName);
        }
    }

    private static void encryptWithStore(String planText, String entry, String entryPasswd, String fileName, String protectedPasswd) throws NoSuchAlgorithmException, CertificateException, KeyStoreException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, UnrecoverableKeyException {
        Objects.requireNonNull(planText, "planText required");
        if (entry == null)
            entry = KEYSTORE_ENTRY;
        if (entryPasswd == null)
            entryPasswd = "";
        //System.out.println(planText + ":" + entry + ":" + entryPasswd + ":" + protectedPasswd + ":" + fileName);
        try (FileInputStream fis = new FileInputStream(fileName)) {
            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            keyStore.load(fis, protectedPasswd.toCharArray());
            if (keyStore.containsAlias(entry)) {
                SecretKey secKey = (SecretKey) keyStore.getKey(entry, entryPasswd.toCharArray());
                //System.out.println(Base64.toBase64String(secKey.getEncoded()));
                encrypt(entry, planText, secKey, Base64.toBase64String(secKey.getEncoded()));
            }
        } catch (IOException e) {
            System.out.println("Keystore was not exists filename=：" + fileName);
        }
    }

    private static void encrypt(String entry, String planText, String password) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Objects.requireNonNull(planText, "planText required");
        if (password == null)
            password = PasswordService.nextStrongPasswd();
        KeyGenerator gen = KeyGenerator.getInstance("AES");
        gen.init(256, new SecureRandom(password.getBytes(StandardCharsets.UTF_8)));
        SecretKey secretKey = gen.generateKey();
        encrypt(entry, planText, secretKey, password);
    }

    private static void encrypt(String entry, String planText, SecretKey key, String password) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] aesData = AESUtil.encryptSpec(planText.getBytes(StandardCharsets.UTF_8), key);
        System.out.println("The planText has encrypted\n" +
                "----------------         -----------------------\n" +
                "entry                    " + entry + "\n" +
                "plan_text                " + planText + "\n" +
                "password                 " + password + "\n" +
                "encrypted(base64)        " + Base64.toBase64String(aesData) + "\n" +
                "encrypted(hex)           " + ByteToHex.toHexStr(aesData)
        );
    }

    /**
     * @return
     */
    public static String nextPasswd() {
        String password = null;
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        boolean isStrong = false;
        while (!isStrong) {
            password = nextPasswd(sb, random);
            if (password.length() >= 8) {
                isStrong = isStrong(password);
            }
        }
        return password;
    }

    /**
     * @return
     */
    public static String nextStrongPasswd() {
        String password = null;
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        boolean isStrong = false;
        while (!isStrong) {
            password = nextPasswd(sb, random);
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
    private static String nextPasswd(StringBuilder password, SecureRandom random) {
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
}
