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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @version 0.0.3 2023-01-10
 * @since JDK8.0
 */
public class ResourceWherePath {
    private static final String PARENT_SEPARATOR_PATH = ".." + File.separatorChar;
    private static final char SEPARATOR = '.';

    /**
     * <pre>
     * such as base path is "com/cp4j/server/util"
     * if relative path is "a.xml",get "com/cp4j/server/util/a.xml"
     * if relative path is "../a.xml",get "com/cp4j/server/a.xml"
     * if relative path is"../../a.xml",get "com/cp4j/a.xml"
     * </pre>
     *
     * @param basePath
     * @param relativePath
     * @return correct file path in line with the current system
     */

    public static String getAbsolutePath(String basePath, String relativePath) {
        if (relativePath.startsWith(String.valueOf(File.separatorChar))) {
            relativePath = relativePath.substring(1);
        }
        int lastIndex = relativePath.lastIndexOf(PARENT_SEPARATOR_PATH);
        int length = PARENT_SEPARATOR_PATH.length();
        // calculate "../" number
        int count = lastIndex / length;
        // remove all "../",but keep last "/" character
        relativePath = relativePath.substring(lastIndex + length - 1);
        for (int i = count; i >= 0; i--) {
            basePath = basePath.substring(0, basePath.lastIndexOf(File.separatorChar));
        }
        return basePath + relativePath;
    }

    /**
     * @param args
     * @throws MalformedURLException
     */

    public static void main(String[] args) throws MalformedURLException {
        long start = System.currentTimeMillis();
        System.out.println("beging test path:");
        String serial = '/' + ResourceWherePath.class.getPackage().getName().replace('.', '/');
        System.out.println(serial);
        System.out.println("\nThread.currentThread().getContextClassLoader.getResource(\"\")");
        System.out.println(Thread.currentThread().getContextClassLoader().getResource(""));
        System.out.println("\nResourceWherePath.class.getResource(\"\")");
        System.out.println(ResourceWherePath.class.getResource(""));
        System.out.println("\nResourceWherePath.class.getResource(\"/\")");
        System.out.println(ResourceWherePath.class.getResource("/"));
        System.out.println("\nResourceWherePath.class.getClassLoader().getResource(\"\")");
        System.out.println(ResourceWherePath.class.getClassLoader().getResource(""));
        System.out.println("\nResourceWherePath.class.getResource(NtpMessage.class)");
        System.out.println(ResourceWherePath.class.getResource("NtpMessage.class"));
        System.out.println("\nClassLoader.getSystemResource(\"\")");
        System.out.println(ClassLoader.getSystemResource(""));

        System.out.println("\nResourceBundle.getBundle(\"FileWatchDog\") ");
        System.out.println(ResourceBundle.getBundle("watch_dog"));
        System.out.println("\nResourceWherePath.toUniversalFilePath(\"salt.hoprxi.utils.NLS.class\")");
        System.out.println(ResourceWherePath.toUniversalFilePath("salt.hoprxi.utils.NLS.class"));
        System.out.println("\nSystem.getProperty(\"user.dir\")");
        System.out.println(System.getProperty("user.dir"));
        System.out.println("\nResourceWherePath.getAbsolutePath(\"file:/d:/s/a/y\",\"../../../sd.xml\")");
        System.out.println(ResourceWherePath.getAbsolutePath("file:" + File.separatorChar + "d:" + File.separatorChar + "s" + File.separatorChar + "a" + File.separatorChar + "y", ".." + File.separatorChar + ".." + File.separatorChar + ".." + File.separatorChar + "sd.xml"));
        //System.out.println(ResourceWherePath.getAbsolutePath(Thread.currentThread().getContextClassLoader().getResource("").toString(), ".." + File.separatorChar + ".." + File.separatorChar + ".." + File.separatorChar + "sd.xml"));
        System.out.println("\nResourceWherePath.toURL(\"salt/hoprxi/utils/Version.class\")");
        System.out.println(ResourceWherePath.toUrl("salt/hoprxi/utils/Version.class"));
        System.out.println("\nResourceWherePath.toUrlWithPoint(\"salt.hoprxi.utils.Version.class\"))");
        System.out.println(ResourceWherePath.toUrlWithPoint("salt.hoprxi.utils.Version.class"));
        System.out.println("\nExecute time:" + (System.currentTimeMillis() - start));
    }

    /**
     * @param resource
     * @return content relative path or the current path if resource is empty
     */
    private static URL toSimpleURL(String resource) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (null != classLoader) {
            // logger.debug("Not find [" + resource
            // + "] using context classloader." + classLoader);
            return classLoader.getResource(resource);
        }
        // Next, try to locate this resource through this class's
        // classloader, such as this.getClass(0.getClassloader()
        classLoader = ResourceWherePath.class.getClassLoader();
        if (null != classLoader) {
            // logger.debug("Not find [" + resource + "] using " + classLoader
            // + " class loader.");
            return classLoader.getResource(resource);
        }
        // Next, try to locate this resource through the system classloader
        // logger.debug("Not find [" + resource
        // + "] using ClassLoader.getSystemResource().");
        return ClassLoader.getSystemResource(resource);
    }

    /**
     * <pre>
     * Begin:  xxx.xxx.xxx.zzz.zzz
     * Result: xxx\xxx\xxx\zzz.zzz
     * file separator "\" depending on the system with different
     * </pre>
     *
     * @param fileName
     * @return
     */
    public static String toUniversalFilePath(String fileName) {
        int i = fileName.lastIndexOf(String.valueOf(SEPARATOR));
        String prefix = fileName.substring(0, i);
        String suffix = fileName.substring(i);
        return prefix.replace(SEPARATOR, File.separatorChar) + suffix;
    }

    /**
     * @param resource
     * @return resource url description
     */
    public static URL toUrl(String resource) {
        resource = Objects.requireNonNull(resource, "resource is required");
        if (resource.contains(PARENT_SEPARATOR_PATH)) {
            try {
                String absolutePath = getAbsolutePath(ResourceWherePath.toSimpleURL("").toExternalForm(), resource);
                return new URL(absolutePath);
            } catch (MalformedURLException e) {
                // logger.debug("Not find [" + resource
                // + "] by resource absolute path.", e);
            }
        }
        return ResourceWherePath.toSimpleURL(resource);
    }

    public static URI toUrI(String resource) throws URISyntaxException {
        return toUrl(resource).toURI();
    }

    /**
     * @param resource such as:mi.hoprxi.resources.cache.conf
     * @return
     */
    public static URL toUrlWithPoint(String resource) {
        return toUrl(toUniversalFilePath(resource));
    }
}
