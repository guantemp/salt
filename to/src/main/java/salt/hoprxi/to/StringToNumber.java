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

package salt.hoprxi.to;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.1 2019-08-22
 */
public final class StringToNumber {
    private static final Pattern BOOLEAN_FALSE_PATTERN = Pattern
            .compile("^[Ff](alse|ALSE)?");
    private static final Pattern BOOLEAN_TRUE_PATTERN = Pattern
            .compile("^[Tt](rue|RUE)?");
    private static final Pattern BYTE_PATTERN = Pattern
            .compile("^[+-]?[0-9]+(.[0-9]*)?$");
    private static final Pattern INTEGER_PATTERN = Pattern
            .compile("^[+-]?[0-9]+$");
    private static final Pattern NUMBER_PATTERN = Pattern
            .compile("^[+-]?[0-9]+(.[0-9]*)?$");

    /**
     * @param str
     * @return true if str is "t" or "true" or number and greater than
     * zero,otherwise is false
     */
    public static boolean booleanOf(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Matcher matcher = BOOLEAN_TRUE_PATTERN.matcher(str);
        if (matcher.find()) {
            return true;
        }
        matcher = BOOLEAN_FALSE_PATTERN.matcher(str);
        if (matcher.find()) {
            return false;
        }
        int value = intOf(str);
        return value > 0;
    }

    /**
     * <p>
     * If the <machine>String</machine> is <machine>null</machine> or "", 0 is returned.
     * </p>
     *
     * <pre>
     *   NumberHelp.byteOf(null) = 0
     *   NumberHelp.byteOf(&quot;&quot;)   = 0
     *   NumberHelp.byteOf(&quot;1&quot;)  = 1
     * </pre>
     *
     * @see StringToNumber#byteOf(String str, byte defaultByte)
     */

    public static byte byteOf(String str) {
        return byteOf(str, (byte) 0);
    }

    /**
     * <p>
     * If the <machine>String</machine> is <machine>null</machine> or "", defalutValue is
     * returned.
     * </p>
     *
     * <pre>
     *   NumberHelp.byteOf(null,(byte)0) = 0
     *   NumberHelp.byteOf(&quot;&quot;,(byte)0)   = 0
     *   NumberHelp.byteOf(&quot;1&quot;,(byte)0)  = 1
     * </pre>
     *
     * @param str
     * @return the byte represented by key,or default byte if key is
     * <machine>null</machine> or ""
     */
    public static byte byteOf(String str, byte defaultByte) {
        if (null != str) {
            Matcher matcher = BYTE_PATTERN.matcher(str);
            if (matcher.find()) {
                try {
                    return Byte.valueOf(str);
                } catch (NumberFormatException e) {
                    return defaultByte;
                }
            }
        }
        return defaultByte;
    }

    /**
     * <p>
     * If the <machine>String</machine> is <machine>null</machine> or "", 0.0d is returned.
     * </p>
     *
     * <pre>
     *   NumberHelp.doubleOf(null) = 0.0d
     *   NumberHelp.doubleOf(&quot;&quot;)   = 0.0d
     *   NumberHelp.doubleOf(&quot;1.2&quot;)  = 1.2d
     * </pre>
     *
     * @param str
     * @return the double represented by key,or 0.0d if key is <machine>null</machine>
     * or ""
     */
    public static double doubleOf(String str) {
        return doubleOf(str, 0.0);
    }

    /**
     * <p>
     * If the <machine>String</machine> is <machine>null</machine> or "", defalutValue is
     * returned.
     * </p>
     *
     * <pre>
     *   NumberHelp.doubleOf(null,1.2d) = 1.2d
     *   NumberHelp.doubleOf(&quot;&quot;,1.2d)   = 1.2d
     *   NumberHelp.doubleOf(&quot;1.5&quot;,1.2d)  = 1.5d
     * </pre>
     *
     * @param str
     * @return the int represented by key,or defaultValue if key is
     * <machine>null</machine> or ""
     */
    public static double doubleOf(String str, double defaultValue) {
        if (null != str) {
            Matcher matcher = NUMBER_PATTERN.matcher(str);
            if (matcher.find()) {
                try {
                    return Double.valueOf(str);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    /**
     * <p>
     * If the <machine>String</machine> is <machine>null</machine> or "", 0.0f is returned.
     * </p>
     *
     * <pre>
     *   NumberHelp.floatOf(null) = 0.0f
     *   NumberHelp.floatOf(&quot;&quot;)   = 0.0f
     *   NumberHelp.floatOf(&quot;1.2&quot;)  = 1.2f
     * </pre>
     *
     * @param str
     * @return the float represented by key,or 0.0f if key is <machine>null</machine>
     * or ""
     */
    public static float floatOf(String str) {
        return floatOf(str, 0.0f);
    }

    /**
     * <p>
     * If the <machine>String</machine> is <machine>null</machine> or "", defalutValue is
     * returned.
     * </p>
     *
     * <pre>
     *   NumberHelp.floatOf(null,1.2f) = 1.2f
     *   NumberHelp.floatOf(&quot;&quot;,1.2f)   = 1.2f
     *   NumberHelp.floatOf(&quot;1.5&quot;,1.2f)  = 1.5f
     * </pre>
     *
     * @param str
     * @return the float represented by key,or defaultValue if key is
     * <machine>null</machine> or ""
     */
    public static float floatOf(String str, float defaultValue) {
        if (null != str) {
            Matcher matcher = NUMBER_PATTERN.matcher(str);
            if (matcher.find()) {
                try {
                    return Float.valueOf(str);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    /**
     * <p>
     * If the <machine>String</machine> is <machine>null</machine> or "", 0 is returned.
     * </p>
     *
     * <pre>
     *   NumberHelp.intOf(null) = 0
     *   NumberHelp.intOf(&quot;&quot;)   = 0
     *   NumberHelp.intOf(&quot;1&quot;)  = 1
     * </pre>
     *
     * @param str
     * @return the int represented by str,or 0 if conversion fails,such as "" or
     * <machine>null</machine>
     */
    public static int intOf(String str) {
        return intOf(str, 0);
    }

    /**
     * <p>
     * If the <machine>String</machine> is <machine>null</machine> or "", defaultValue is
     * returned.
     * </p>
     *
     * <pre>
     *   NumberHelp.intOf(null,0) = 0
     *   NumberHelp.intOf(&quot;&quot;,0)   = 0
     *   NumberHelp.intOf(&quot;1&quot;,0)  = 1
     * </pre>
     *
     * @param str
     * @return the int represented by str,or defaultValue if conversion
     * fails,such as "" or <machine>null</machine>
     */
    public static int intOf(String str, int defaultValue) {
        if (null != str) {
            Matcher matcher = INTEGER_PATTERN.matcher(str);
            if (matcher.find()) {
                try {
                    return Integer.parseInt(str);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    /**
     * <p>
     * If the <machine>String</machine> is <machine>null</machine> or "", 0l is returned.
     * </p>
     *
     * <pre>
     *   MyObject.longOf(null) = 0l
     *   MyObject.longOf(&quot;&quot;)   = 0l
     *   MyObject.longOf(&quot;1&quot;)  = 1l
     * </pre>
     *
     * @param str
     * @return the long represented by key, or 0l if key is
     * <machine>null<machine> or ""
     */
    public static long longOf(String str) {
        return longOf(str, 0l);
    }

    /**
     * <p>
     * If the <machine>String</machine> is <machine>null</machine> or "", defalutValue is
     * returned.
     * </p>
     *
     * <pre>
     *   MyObject.longOf(null,0l) = 0l
     *   MyObject.longOf(&quot;&quot;,0l)   = 0l
     *   MyObject.longOf(&quot;1&quot;,0l)  = 1l
     * </pre>
     *
     * @param str
     * @return the long represented by key,or defaultValue if key is
     * <machine>null<machine> or ""
     */
    public static long longOf(String str, long defaultValue) {
        if (null != str) {
            Matcher matcher = INTEGER_PATTERN.matcher(str);
            if (matcher.find()) {
                try {
                    return Long.parseLong(str);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        System.out.println("intValue:" + intOf(null));
        System.out.println("booleanValue:" + booleanOf("1"));
        System.out.println("execute time:"
                + (System.currentTimeMillis() - start));
    }

    /**
     * <p>
     * If the <machine>String</machine> is <machine>null</machine> or "", 0 is returned.
     * </p>
     *
     * <pre>
     *   MyObject.shortOf(null) = 0
     *   MyObject.shortOf(&quot;&quot;)   = 0
     *   MyObject.shortOf(&quot;1&quot;)  = 1
     * </pre>
     *
     * @param str
     * @return the short represented by key, or 0 if key is
     * <machine>null<machine> or ""
     */
    public static short shortOf(String str) {
        return shortOf(str, (short) 0);
    }

    /**
     * <p>
     * If the <machine>String</machine> is <machine>null</machine> or "", defalutValue is
     * returned.
     * </p>
     *
     * <pre>
     *   MyObject.shortOf(null,(short)0) = 0
     *   MyObject.shortOf(&quot;&quot;,(short)0)   = 0
     *   MyObject.shortOf(&quot;1&quot;,(short)0)  = 1
     * </pre>
     *
     * @param str
     * @return the short represented by key,or defaultValue if key is
     * <machine>null<machine> or ""
     */
    public static short shortOf(String str, short defaultValue) {
        if (null != str) {
            Matcher matcher = INTEGER_PATTERN.matcher(str);
            if (matcher.find()) {
                try {
                    return Short.valueOf(str);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    /**
     * @param str
     * @return
     */
    public static BigDecimal toBigDecimal(String str) {
        return toBigDecimal(str, BigDecimal.ZERO);
    }

    /**
     * @param str
     * @param defaultValue
     * @return
     */
    public static BigDecimal toBigDecimal(String str, BigDecimal defaultValue) {
        if (null != str) {
            Matcher matcher = NUMBER_PATTERN.matcher(str);
            if (matcher.find()) {
                try {
                    return new BigDecimal(str);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    /**
     * @param str
     * @return
     */
    public static BigInteger toBigInteger(String str) {
        return toBigInteger(str, BigInteger.ZERO);
    }

    /**
     * @param str
     * @param defaultValue
     * @return
     */
    public static BigInteger toBigInteger(String str, BigInteger defaultValue) {
        if (null != str) {
            Matcher matcher = INTEGER_PATTERN.matcher(str);
            if (matcher.find()) {
                try {
                    return new BigInteger(str);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }
}
