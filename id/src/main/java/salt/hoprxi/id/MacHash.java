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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.Objects;

/**
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @version 0.0.2 2019-07-10
 * @since JDK8.0
 */
public final class MacHash {
    private static final Logger LOGGER = LoggerFactory.getLogger(MacHash.class);
    private static int hash;

    static {
        StringBuilder sb = new StringBuilder();
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                sb.append(ni.getName()).append(macAddress(ni.getHardwareAddress()));
            }
            hash = Objects.hash(sb);
        } catch (SocketException e) {
            hash = (new SecureRandom().nextInt());
            LOGGER.warn("Not find network card");
        }
    }


    /**
     * @param mac
     * @return
     */
    private static String macAddress(byte[] mac) {
        if (mac == null)
            return "00:00:00:00:00:00";
        // Assemble MAC addresses into String
        StringBuilder sb = new StringBuilder();
        for (int i = 0, j = mac.length; i < j; i++) {
            if (i != 0) {
                sb.append(":");
            }
            // mac[i] & 0xFF Byte Converts to Positive Integer
            String s = Integer.toHexString(mac[i] & 0xFF);
            sb.append(s.length() == 1 ? 0 + s : s);
        }
        return sb.toString().toUpperCase();
    }

    /**
     * get mac address
     *
     * @param ia
     * @return
     * @throws Exception
     */
    public static String macAddress(InetAddress ia) throws Exception {
        byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
        return macAddress(mac);
    }

    /**
     * @return Mixed mac address, the title of the signature process line
     */
    public static int hash() {
        return hash;
    }
}
