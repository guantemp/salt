/*
 * Copyright (c) 2024. www.hoprxi.com All Rights Reserved.
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

package salt.hoprxi.crypto.util;

import org.testng.annotations.Test;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2024-09-24
 */
public class StoreKeyLoadTest {

    @Test
    public void testLoadSecretKey() {
        StoreKeyLoad.loadSecretKey("keystore.jks", "Qwe123465",
                new String[]{"120.77.47.145:6379:P$Qwe123465Re", "125.68.186.195:9200:P$Qwe123465El", "120.77.47.145:5432:P$Qwe123465Pg", "125.68.186.195:5432:P$Qwe123465Pg"});
    }

    @Test(priority = 1)
    public void testDecrypt() {
        System.out.println(StoreKeyLoad.decrypt("120.77.47.145:6379", "yqjbhVxP8riIK34bbYdSIskltdUNBo8pmfVgbvt0uLo="));
        System.out.println(StoreKeyLoad.decrypt("120.77.47.145:6379", "k2vrFVzW5x3JGZ7Qh4cnju0DGtm37Eabp47aBnWMVHo="));
    }
}