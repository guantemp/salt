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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ResourceBundle;


/***
 * @author <a href="www.hoprx.com.cc/authors/guan xiangHuan">guan xiangHuan</a>
 * @since JDK8.0
 * @version 0.0.2 2022-03-18
 */
public class NLS {
    private static Logger logger = LoggerFactory.getLogger(NLS.class);

    /**
     * all type is static string will load from bundle resourece
     *
     * @param bundleName
     * @param bundleClass
     */
    protected static void initializeMessages(String bundleName, Class<?> bundleClass) {
        Field[] allFields = bundleClass.getFields();
        ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
        for (Field field : allFields) {
            int modifier = field.getModifiers();
            if (field.getType().equals(String.class) && Modifier.isStatic(modifier) && !Modifier.isFinal(modifier)) {
                try {
                    field.set(null, bundle.getString(field.getName()));
                } catch (IllegalAccessException e) {
                    logger.error("You must modify {} permissions to public static", field.getName());
                }
            }
        }
    }

    protected static void initializeMessages(File resourceFile, Class<?> bundleClass) {

    }

    protected static void initializeImages(String bundleName, Class<?> bundleClass) {
        Field[] allFields = bundleClass.getFields();
        ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
    }
}
