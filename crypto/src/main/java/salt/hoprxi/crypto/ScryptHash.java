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


import salt.hoprxi.crypto.algorithms.Scrypt;

/***
 * @author <a href="www.hoprxi.com/author/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.1 2019-04-15
 */
public class ScryptHash implements HashService {
    //CPU消耗参数，必须大于1, 是2的n次幂并且小于2^(128 * r / 8)
    private static final int N = 16384;
    //块容量参数
    private static final int R = 8;
    //并行化参数，一个小于等于((2^32-1) * hLen) / MFLen的正整数，其中hLen为 32，MFlen是128 * r。（在ltc协议中 p = 1）
    private static final int P = 1;
    //SALT LENGTH
    private static final int S = 32;

    @Override
    public String hash(String plainText) {
        return Scrypt.scrypt(plainText, S, N, R, P);
    }

    @Override
    public boolean check(String plainText, String securedPlainTextHash) {
        return Scrypt.verify(plainText, securedPlainTextHash);
    }
}
