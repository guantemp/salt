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

package salt.hoprxi.utils;

import org.testng.annotations.Test;

import java.io.File;
import java.util.ResourceBundle;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2024-04-06
 */
public class ResourceWhereTest {

    @Test
    public void testGetAbsolutePath() {
        System.out.println("beging test path:");
        System.out.println(ResourceWhere.toUniversalFilePath( "watch_dog.properties"));
        System.out.println(ResourceWhere.toUrl( "watch_dog.properties"));
    }

    @Test
    public void testToUniversalFilePath() {
        String serial = '/' + ResourceWhere.class.getPackage().getName().replace('.', '/');
        System.out.println(serial);
        System.out.println("\nThread.currentThread().getContextClassLoader.getResource(\"\")");
        System.out.println(Thread.currentThread().getContextClassLoader().getResource(""));
        System.out.println("\nResourceWherePath.class.getResource(\"\")");
        System.out.println(ResourceWhere.class.getResource(""));
        System.out.println("\nResourceWherePath.class.getResource(\"/\")");
        System.out.println(ResourceWhere.class.getResource("/"));
        System.out.println("\nResourceWherePath.class.getClassLoader().getResource(\"\")");
        System.out.println(ResourceWhere.class.getClassLoader().getResource(""));
        System.out.println("\nResourceWherePath.class.getResource(NtpMessage.class)");
        System.out.println(ResourceWhere.class.getResource("NtpMessage.class"));
        System.out.println("\nClassLoader.getSystemResource(\"\")");
        System.out.println(ClassLoader.getSystemResource(""));

        System.out.println("\nResourceBundle.getBundle(\"FileWatchDog\") ");
        System.out.println(ResourceBundle.getBundle("watch_dog"));
        System.out.println("\nResourceWherePath.toUniversalFilePath(\"salt.hoprxi.utils.NLS.class\")");
        System.out.println(ResourceWhere.toUniversalFilePath("salt.hoprxi.utils.NLS.class"));
        System.out.println("\nSystem.getProperty(\"user.dir\")");
        System.out.println(System.getProperty("user.dir"));
        System.out.println("\nResourceWherePath.getAbsolutePath(\"file:/d:/s/a/y\",\"../../../sd.xml\")");
        System.out.println(ResourceWhere.getAbsolutePath("file:" + File.separatorChar + "d:" + File.separatorChar + "s" + File.separatorChar + "a" + File.separatorChar + "y", ".." + File.separatorChar + ".." + File.separatorChar + ".." + File.separatorChar + "sd.xml"));
        //System.out.println(ResourceWherePath.getAbsolutePath(Thread.currentThread().getContextClassLoader().getResource("").toString(), ".." + File.separatorChar + ".." + File.separatorChar + ".." + File.separatorChar + "sd.xml"));
        System.out.println("\nResourceWherePath.toURL(\"salt/hoprxi/utils/Version.class\")");
        System.out.println(ResourceWhere.toUrl("salt/hoprxi/utils/Version.class"));
        System.out.println("\nResourceWherePath.toUrlWithPoint(\"salt.hoprxi.utils.Version.class\"))");
        System.out.println(ResourceWhere.toUrlWithPoint("salt.hoprxi.utils.Version.class"));

    }

    @Test
    public void testToUrl() {
    }
}