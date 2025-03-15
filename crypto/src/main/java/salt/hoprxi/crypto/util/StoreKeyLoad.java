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

package salt.hoprxi.crypto.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2024-09-21
 */
public final class StoreKeyLoad {
    public static final Map<String, SecretKey> SECRET_KEY_PARAMETER = new HashMap<>();
    private static final Pattern PASS = Pattern.compile("^P\\$.*");
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreKeyLoad.class);

    public static void loadSecretKey(String keystoreFile, String keystoreFileProtectedPasswd, String[] entries) {
        //System.out.println(Arrays.toString(entries));
        try (InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(keystoreFile)) {
            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            keyStore.load(fis, keystoreFileProtectedPasswd.toCharArray());
            Enumeration<String> alias = keyStore.aliases();
            while (alias.hasMoreElements()) {
                String entry = alias.nextElement();
                if (!isDuplicates(entries, entry))
                    entries = Stream.concat(Arrays.stream(entries), Stream.of(entry)).toArray(String[]::new);
            }
            for (String entry : entries) {
                String[] ss = entry.split(":");
                String rowPass = "";
                if (PASS.matcher(ss[ss.length - 1]).matches()) {
                    rowPass = ss[ss.length - 1].substring(2);
                    ss = Arrays.copyOf(ss, ss.length - 1);
                }
                //System.out.println(Arrays.toString(ss));
                if (ss.length == 3)//https://slave.tooo.top:9200
                    SECRET_KEY_PARAMETER.put(ss[0] + ":" + ss[1] + ":" + ss[2], (SecretKey) keyStore.getKey(ss[0] + ":" + ss[1] + ":" + ss[2], rowPass.toCharArray()));
                if (ss.length == 2) //125.68.186.195:5432,slave.tooo.top:9200
                    SECRET_KEY_PARAMETER.put(ss[0] + ":" + ss[1], (SecretKey) keyStore.getKey(ss[0] + ":" + ss[1], rowPass.toCharArray()));
                if (ss.length == 1)
                    SECRET_KEY_PARAMETER.put(ss[0], (SecretKey) keyStore.getKey(ss[0], rowPass.toCharArray()));
            }
        } catch (FileNotFoundException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            LOGGER.error("Not find key store file in {}", keystoreFile, e);
        } catch (IOException e) {
            LOGGER.error("Keystore protected password was incorrect {}", keystoreFileProtectedPasswd, e);
        } catch (UnrecoverableKeyException e) {
            LOGGER.error("Is a bad key is used during decryption", e);
        }
    }

    private static boolean isDuplicates(String[] entries, String savedEntry) {
        for (String entry : entries) {
            if (entry.replaceFirst(":P\\$.*", "").equalsIgnoreCase(savedEntry)) {
                return true;
            }
        }
        return false;
    }

    public static String decrypt(String entry, String securedPlainText) {
        byte[] aesData = Base64.getDecoder().decode(securedPlainText);
        //Bootstrap.SECRET_KEY_MAP.get(entry);
        try {
            byte[] decryptData = AESUtil.decryptSpec(aesData, StoreKeyLoad.SECRET_KEY_PARAMETER.get(entry));
            return new String(decryptData, StandardCharsets.UTF_8);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            LOGGER.error("Decrypt entry={},securedPlainText={} exception", entry, securedPlainText, e);
            throw new RuntimeException(String.format("decrypt entry=%s,securedPlainText=%s exception", entry, securedPlainText), e);
        }
    }
}
