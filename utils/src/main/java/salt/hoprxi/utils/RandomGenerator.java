/*
 * Copyright (c) 2023. www.hoprxi.com All Rights Reserved.
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
package salt.hoprxi.utils;

import java.security.SecureRandom;

/**
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @version 0.0.1 2020-01-07
 * @since JDK8.0
 */
public enum RandomGenerator {
    CHARACTER {
        private static final String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.*?[](),{}^";

        @Override
        public String generate() {
            return generate(6);
        }

        @Override
        public String generate(int length) {
            SecureRandom random = new SecureRandom();
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < length; i++) {
                int num = random.nextInt(69);
                buf.append(str.charAt(num));
            }
            return buf.toString();
        }
    },
    PHONE {
        @Override
        public String generate() {
            return String.valueOf((int) ((Math.random() * 10 + 1) * 1000000000000L));
        }

        @Override
        public String generate(int length) {
            return generate();
        }

    }, SIX_NUMBER {
        @Override
        public String generate() {
            return String.valueOf((int) ((Math.random() * 6 + 1) * 100000));
        }

        @Override
        public String generate(int length) {
            return generate();
        }

    }, FOUR_NUMBER {
        @Override
        public String generate() {
            return String.valueOf((int) ((Math.random() * 4 + 1) * 1000));
        }

        @Override
        public String generate(int length) {
            return generate();
        }
    }, CHINESE {
        @Override
        public String generate() {
            return generate(6);
        }

        @Override
        public String generate(int length) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                sb.append((char) (0x4e00 + (int) (Math.random() * (0x9fa5 - 0x4e00 + 1))));
            }
            return sb.toString();
        }

    };

    public abstract String generate();

    public abstract String generate(int length);
}
