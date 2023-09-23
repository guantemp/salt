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

package salt.hoprxi.to;

import java.util.Optional;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.2 2020-05-05
 */
public final class DigitalToChinese {
    //小写中文数字
    private static final String[] LOWERCASE_DIGITS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    //大写中文数字
    private static final String[] CAPITAL_DIGITS = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    //小写中文单位
    private static final String[] LOWERCASE_UNITS = {"", "十", "百", "千"};
    // 大写中文单位
    private static final String[] CAPITAL__UNITS = {"", "拾", "佰", "仟"};


    /**
     * 阿拉伯数字转换成中文,小数点后四舍五入保留两位. 使用于整数、小数的转换.
     *
     * @param amount           数字
     * @param isUseTraditional 是否使用繁体
     * @return 中文
     */
    public static String format(double amount, boolean isUseTraditional) {
        return format(amount, isUseTraditional, false);
    }

    public static String formatRMB(double amount) {
        return "";
    }

    /**
     * 阿拉伯数字转换成中文,小数点后四舍五入保留两位. 使用于整数、小数的转换.
     *
     * @param amount           数字
     * @param isUseTraditional 是否使用繁体
     * @param isMoneyMode      是否为金额模式
     * @return 中文
     */
    public static String format(double amount, boolean isUseTraditional, boolean isMoneyMode) {
        final String[] numArray = isUseTraditional ? CAPITAL_DIGITS : LOWERCASE_DIGITS;

        if (amount > 99999999999999.99 || amount < -99999999999999.99) {
            throw new IllegalArgumentException("Number support only: (-99999999999999.99 ～ 99999999999999.99)！");
        }

        boolean negative = false;
        if (amount < 0) {
            negative = true;
            amount = -amount;
        }

        long temp = Math.round(amount * 100);
        int numFen = (int) (temp % 10);
        temp = temp / 10;
        int numJiao = (int) (temp % 10);
        temp = temp / 10;

        //将数字以万为单位分为多份
        int[] parts = new int[20];
        int numParts = 0;
        for (int i = 0; temp != 0; i++) {
            int part = (int) (temp % 10000);
            parts[i] = part;
            numParts++;
            temp = temp / 10000;
        }

        boolean beforeWanIsZero = true; // 标志“万”下面一级是不是 0

        StringBuilder chineseStr = new StringBuilder();
        for (int i = 0; i < numParts; i++) {
            String partChinese = toChinese(parts[i], isUseTraditional);
            if (i % 2 == 0) {
                beforeWanIsZero = partChinese == null || partChinese.isEmpty();
            }

            if (i != 0) {
                if (i % 2 == 0) {
                    chineseStr.insert(0, "亿");
                } else {
                    if ("".equals(partChinese) && !beforeWanIsZero) {
                        // 如果“万”对应的 part 为 0，而“万”下面一级不为 0，则不加“万”，而加“零”
                        chineseStr.insert(0, "零");
                    } else {
                        if (parts[i - 1] < 1000 && parts[i - 1] > 0) {
                            // 如果"万"的部分不为 0, 而"万"前面的部分小于 1000 大于 0， 则万后面应该跟“零”
                            chineseStr.insert(0, "零");
                        }
                        chineseStr.insert(0, "万");
                    }
                }
            }
            chineseStr.insert(0, partChinese);
        }

        // 整数部分为 0, 则表达为"零"
        if (chineseStr.toString().isEmpty()) {
            chineseStr = Optional.ofNullable(numArray[0]).map(StringBuilder::new).orElse(null);
        }
        //负数
        if (negative) { // 整数部分不为 0
            chineseStr = (chineseStr == null ? new StringBuilder("null") : chineseStr).insert(0, "负");
        }

        // 小数部分
        if (numFen != 0 || numJiao != 0) {
            if (numFen == 0) {
                chineseStr = (chineseStr == null ? new StringBuilder("null") : chineseStr).append(isMoneyMode ? "元" : "点").append(numArray[numJiao]).append(isMoneyMode ? "角" : "");
            } else { // “分”数不为 0
                if (numJiao == 0) {
                    chineseStr = (chineseStr == null ? new StringBuilder("null") : chineseStr).append(isMoneyMode ? "元零" : "点零").append(numArray[numFen]).append(isMoneyMode ? "分" : "");
                } else {
                    chineseStr = (chineseStr == null ? new StringBuilder("null") : chineseStr).append(isMoneyMode ? "元" : "点").append(numArray[numJiao]).append(isMoneyMode ? "角" : "").append(numArray[numFen]).append(isMoneyMode ? "分" : "");
                }
            }
        } else if (isMoneyMode) {
            //无小数部分的金额结尾
            chineseStr = (chineseStr == null ? new StringBuilder("null") : chineseStr).append("元整");
        }

        return chineseStr == null ? null : chineseStr.toString();

    }

    /**
     * 把一个 0~9999 之间的整数转换为汉字的字符串，如果是 0 则返回 ""
     *
     * @param amountPart       数字部分
     * @param isUseTraditional 是否使用繁体数字
     * @return 转换后的汉字
     */
    private static String toChinese(int amountPart, boolean isUseTraditional) {
//		if (amountPart < 0 || amountPart > 10000) {
//			throw new IllegalArgumentException("Number must 0 < num < 10000！");
//		}

        String[] numArray = isUseTraditional ? CAPITAL_DIGITS : LOWERCASE_DIGITS;
        String[] units = isUseTraditional ? CAPITAL__UNITS : LOWERCASE_UNITS;

        int temp = amountPart;

        StringBuilder chineseStr = new StringBuilder();
        boolean lastIsZero = true; // 在从低位往高位循环时，记录上一位数字是不是 0
        for (int i = 0; temp > 0; i++) {
            int digit = temp % 10;
            if (digit == 0) { // 取到的数字为 0
                if (!lastIsZero) {
                    // 前一个数字不是 0，则在当前汉字串前加“零”字;
                    chineseStr.insert(0, "零");
                }
                lastIsZero = true;
            } else { // 取到的数字不是 0
                chineseStr.insert(0, numArray[digit] + units[i]);
                lastIsZero = false;
            }
            temp = temp / 10;
        }
        return chineseStr.toString();
    }
}
