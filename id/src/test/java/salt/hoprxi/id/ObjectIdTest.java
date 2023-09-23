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

package salt.hoprxi.id;

import org.testng.annotations.Test;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2021-08-07
 */
public class ObjectIdTest {

    @Test
    public void id() {
        ObjectId oi = new ObjectId();
        for (int i = 0; i < 1000000; i++) {
            oi.generate().id();
        }
    }

    @Test
    void out() {
        ObjectId oi = new ObjectId();
        System.out.println("Machine：" + MacHash.hash());
        System.out.println("Process id：" + Process.process());
        for (int i = 0; i < 20; i++) {
            System.out.println(oi.generate().id());
        }
    }

    @Test
    public void timestamp() {
        ObjectId id = new ObjectId();
        System.out.println(id.timestamp());
    }
}