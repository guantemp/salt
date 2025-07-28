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

package salt.hoprxi.cache.util;


import org.openjdk.jol.info.ClassLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK21
 * @version 0.0.1 builder 2025/7/28
 */
 
public class LayoutCache {
    private static final Map<Class<?>, ClassLayout> CLASS_LAYOUT_CACHE = new ConcurrentHashMap<>();

    static {
        // 预加载核心类
        cacheLayout(String.class);
        cacheLayout(Integer.class);
        cacheLayout(Long.class);
        cacheLayout(ArrayList.class);
        cacheLayout(HashMap.class);
        cacheLayout(ConcurrentHashMap.class);
        cacheLayout(HashSet.class);
    }

    public static ClassLayout getLayout(Class<?> clazz) {
        return CLASS_LAYOUT_CACHE.computeIfAbsent(clazz, ClassLayout::parseClass);
    }

    public static void cacheLayout(Class<?> clazz) {
        CLASS_LAYOUT_CACHE.put(clazz, ClassLayout.parseClass(clazz));
    }
}
