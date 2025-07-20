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

package salt.hoprxi.crypto.application;

import salt.hoprxi.crypto.util.StoreKeyLoad;

import java.util.regex.Pattern;

/**
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @version 0.1 2025/7/20
 * @since JDK 21
 */

public final class DatabaseSpecDecrypt {
    private static final Pattern ENCRYPTED = Pattern.compile("^ENC:.*");

    public static String decrypt(String entry, String securedPlainText) {
        if (ENCRYPTED.matcher(securedPlainText).matches()) {
            securedPlainText = securedPlainText.split(":")[1];
            return StoreKeyLoad.decrypt(entry, securedPlainText);
        }
        return securedPlainText;
    }
}
