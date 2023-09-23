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


import salt.hoprxi.crypto.algorithms.SM3Digest;
import salt.hoprxi.to.ByteToHex;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 2019-08-12
 */
public class SM3Hash implements HashService {
    @Override
    public String hash(String plainText) {
        byte[] result = hash(plainText.getBytes(StandardCharsets.UTF_8));
        return ByteToHex.toHexStr(result);
    }

    @Override
    public boolean check(String plainText, String securedPlainTextHash) {
        byte[] plain = hash(plainText.getBytes(StandardCharsets.UTF_8));
        byte[] securedPlain = ByteToHex.toBytes(securedPlainTextHash);
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
