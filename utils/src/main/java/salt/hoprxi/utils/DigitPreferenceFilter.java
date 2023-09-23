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

package salt.hoprxi.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @version 0.0.1 2020-05-05
 * @since JDK8.0
 */
public class DigitPreferenceFilter {
    private static final Pattern DIGIT_PATTERN = Pattern.compile("^[+|-]?\\d*$");

    /**
     * @param s
     * @param filter
     * @return
     */
    public static boolean mantissaPreferenceFilter(CharSequence s, int[] filter) {
        Matcher matcher = DIGIT_PATTERN.matcher(s);
        if (!matcher.matches())
            throw new IllegalArgumentException("s ");
        for (int i : filter)
            if ((s.charAt(s.length() - 1) - '0') == i)
                return false;
        return true;
    }

    /**
     * @param l
     * @param filter
     * @return
     */
    public static boolean mantissaPreferenceFilter(long l, int[] filter) {
        int i = (int) l % 10;
        for (int j : filter)
            if (j == i)
                return false;
        return true;
    }

    /**
     * @param s
     * @param filter
     * @return
     */
    public static boolean preferenceFilter(CharSequence s, int[] filter) {
        for (int i : filter) {
            for (int j = s.length() - 1; j >= 0; j--) {
                if ((s.charAt(j) - '0') == i)
                    return false;
            }
        }
        return true;
    }

    /**
     * @param l
     * @param filter
     * @return
     */
    public static boolean preferenceFilter(long l, int[] filter) {
        while (l > 0) {
            int i = (int) l % 10;
            for (int j : filter)
                if (j == i)
                    return false;
            l /= 10;
        }
        return true;
    }
}
