/*
 * Copyright (c) 2020 www.hoprxi.com All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package salt.hoprxi.utils;

import java.util.Random;

/**
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @version 0.0.1 2019-12-07
 * @since JDK8.0
 */
public enum RandomGenerator {
    CHARACTER {
        private static final String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.*?[](),{}^";

        @Override
        public String random(int length) {
            Random random = new Random();
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < length; i++) {
                int num = random.nextInt(69);
                buf.append(str.charAt(num));
            }
            return buf.toString();
        }
    },
    PHONE {
        @Override
        public String random(int length) {
            return String.valueOf((int) ((Math.random() * 9 + 1) * 1000000000000l));
        }
    }, SIX_NUMBER {
        @Override
        public String random(int length) {
            return String.valueOf((int) ((Math.random() * 6 + 1) * 100000));
        }
    };

    /**
     * @return
     */
    public abstract String random(int length);
}
