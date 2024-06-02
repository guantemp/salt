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

package salt.hoprxi.crypto.hash;


import org.bouncycastle.crypto.digests.SM3Digest;
import salt.hoprxi.crypto.HashService;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.2 2024-05-20
 */
public class SM3Hash implements HashService {
    @Override
    public String hash(String plainText) {
        byte[] result = hash(plainText.getBytes(StandardCharsets.UTF_8));
        //System.out.println(Base64.toBase64String(result));
        return Base64.getEncoder().encodeToString(result);
    }

    @Override
    public boolean matches(String plainText, String securedPlainTextHash) {
        byte[] plain = hash(plainText.getBytes(StandardCharsets.UTF_8));
        byte[] securedPlain = Base64.getDecoder().decode(securedPlainTextHash);
        return Arrays.equals(plain, securedPlain) ? true : false;
    }

    private byte[] hash(byte[] bytes) {
        SM3Digest digest = new SM3Digest();
        digest.update(bytes, 0, bytes.length);
        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        return result;
    }
}
