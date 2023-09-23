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

import org.testng.Assert;
import org.testng.annotations.Test;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 2020-05-05
 */
public class DigitPreferenceFilterTest {

    @Test
    public void mantissaPreferenceFilter() {
        Assert.assertFalse(DigitPreferenceFilter.mantissaPreferenceFilter(1234, new int[]{4}));
        Assert.assertTrue(DigitPreferenceFilter.mantissaPreferenceFilter(12345, new int[]{4}));
    }

    @Test
    public void preferenceFilter() {
        Assert.assertFalse(DigitPreferenceFilter.preferenceFilter(1235324, new int[]{4}));
        Assert.assertFalse(DigitPreferenceFilter.preferenceFilter("12353243465", new int[]{4}));
        Assert.assertFalse(DigitPreferenceFilter.preferenceFilter("1235323465", new int[]{4}));
        Assert.assertTrue(DigitPreferenceFilter.preferenceFilter("123532365", new int[]{4}));
    }
}