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


import salt.hoprxi.crypto.algorithms.Bcrypt;

/***
 * @author <a href="www.hoprxi.com/author/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.1 2018-01-02
 */
@Deprecated
public final class BcryptHash implements HashService {
    private static final int SALT = 16;

    @Override
    public boolean matches(String plainText, String securedPlainTextHash) {
        return Bcrypt.checkpw(plainText, securedPlainTextHash);
    }

    @Override
    public String hash(String plainText) {
        return Bcrypt.hashpw(plainText);
    }
}
