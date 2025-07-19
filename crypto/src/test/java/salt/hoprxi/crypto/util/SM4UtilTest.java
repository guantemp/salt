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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.testng.annotations.Test;
import salt.hoprxi.to.ByteToHex;

import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2024-05-29
 */
public class SM4UtilTest {

    private static final String PASSWD = "Qwe123465Dw中文SM4";
    private static SecretKey key;

    static {
        Security.addProvider(new BouncyCastleProvider());
        KeyGenerator kg1;
        try {
            kg1 = KeyGenerator.getInstance("SM4", BouncyCastleProvider.PROVIDER_NAME);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
        //java 9+以上固定的random不会生成固定的kry
        kg1.init(128, new SecureRandom("Qwe13465".getBytes(StandardCharsets.UTF_8)));
        key = kg1.generateKey();
        System.out.println(Base64.getEncoder().encodeToString(key.getEncoded()));
    }

    @Test
    public void testEncrypt() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] sources = PASSWD.getBytes(StandardCharsets.UTF_8);
        System.out.println("加密原文(text:base64):" + PASSWD + "$" + Base64.getEncoder().encodeToString(sources));

        byte[] encrypt = SM4Util.encrypt(sources, key);
        System.out.println("SM4加密结果(hex):" + ByteToHex.toHexStr(encrypt));
        System.out.println("SM4加密结果(base64):" + Base64.getEncoder().encodeToString(encrypt));
    }

    @Test(priority = 1)
    public void testDecrypt() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] decrypt = SM4Util.decrypt(Base64.getDecoder().decode("e/5uY7DynZrHn07hySLaAbANfk6Oi6fx1cuteCX3jqOgITTqwSxG2E1z2RgwHIBq"), key);
        System.out.println("SM4解密结果(base64):" + new String(decrypt, StandardCharsets.UTF_8));
        decrypt = SM4Util.decrypt(ByteToHex.toBytes("7bfe6e63b0f29d9ac79f4ee1c922da01b00d7e4e8e8ba7f1d5cbad7825f78ea3a02134eac12c46d84d73d918301c806a"), key);
        System.out.println("SM4解密结果(base64):" + new String(decrypt, StandardCharsets.UTF_8));
    }
}