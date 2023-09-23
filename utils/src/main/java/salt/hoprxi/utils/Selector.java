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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2023-03-03
 */
public class Selector {
    //weight=1 对应的基本虚拟节点个数
    private int virtualNodeNum = 160;
    private List<Divisor> physicsDivisors = new LinkedList<>();
    private int sum = 0;
    //定义一个0--2^32-1环
    private SortedMap<Integer, Divisor> sortedMap = new TreeMap<>();

    public Selector() {
    }

    public Selector(List<Divisor> divisors) {
        for (Divisor divisor : divisors) {
            sum += divisor.weight;
        }
        if (divisors.size() > 0)
            refreshHashCircle();
    }

    public Selector(Divisor[] divisors) {
        this(Arrays.asList(divisors));
    }

    /**
     * @param divisor add one server
     */
    public void add(Divisor divisor) {
        physicsDivisors.add(divisor);
        sum += divisor.weight;
        refreshHashCircle();
    }

    /**
     * @param divisor del one server
     */
    public void delete(Divisor divisor) {
        physicsDivisors.remove(divisor);
        sum -= divisor.weight;
        refreshHashCircle();
    }

    private void refreshHashCircle() {
        double[] percentage = new double[physicsDivisors.size()];
        for (int i = 0, j = physicsDivisors.size(); i < j; i++) {
            percentage[i] = physicsDivisors.get(i).weight / (double) sum * 100;
        }
        Arrays.sort(percentage);
        double min = percentage[0];
        sortedMap.clear();
        for (Divisor divisor : physicsDivisors) {
            int proportion = (int) Math.round(divisor.weight / (double) sum * 100 / min);
            //System.out.print(proportion + "\t");
            for (int i = 0; i < virtualNodeNum / 4 * proportion; i++) {
                String virtualNodeName = divisor.value + "&&virtual:" + i;
                byte[] bytes = md5(virtualNodeName);
                for (int j = 0; j < 4; j++) {
                    sortedMap.put(ketamaHash(bytes, j), divisor);
                }
            }
        }
    }

    public <T> T select(String key) {
        String virtualNodeName = key + "&&virtual:0";
        byte[] bytes = md5(virtualNodeName);
        int hash = ketamaHash(bytes, 0);
        SortedMap<Integer, Divisor> subMap = sortedMap.tailMap(hash);
        if (subMap == null || subMap.isEmpty()) {
            return (T) (sortedMap.get(sortedMap.firstKey()).value());
        } else {
            return (T) (subMap.get(subMap.firstKey()).value());
        }
    }

    public <T> T select() {
        return select(String.valueOf(ThreadLocalRandom.current().nextInt()));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Selector.class.getSimpleName() + "[", "]")
                .add("sum=" + sum)
                .add("sortedMapSize=" + sortedMap.size())
                .add("sortedMap=" + sortedMap)
                .toString();
    }

    /*
            private static void refreshHashCircle() {
                // 当集群变动时，刷新hash环，其余的集群在hash环上的位置不会发生变动
                virtualNodes.clear();
                for (String realGroup : realGroups) {
                    for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                        String virtualNodeName = getVirtualNodeName(realGroup, i);
                        int hash = HashUtil.getHash(virtualNodeName);
                        System.out.println("[" + virtualNodeName + "] launched @ " + hash);
                        virtualNodes.put(hash, virtualNodeName);
                    }
                }
            }

            private static void addGroup(String identifier) {
                realGroups.add(identifier);
                refreshHashCircle();
            }

            private static void removeGroup(String identifier) {
                int i = 0;
                for (String group : realGroups) {
                    if (group.equals(identifier)) {
                        realGroups.remove(i);
                    }
                    i++;
                }
                refreshHashCircle();
            }


            /**
             * @param data
             * @return
             */
    public int FNVHash1(String data) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < data.length(); i++)
            hash = (hash ^ data.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        //如果算出来的值为负数，则取其绝对值
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }

    /**
     * @param digest
     * @param number
     * @return
     */
    private int ketamaHash(byte[] digest, int number) {
        /*
        long rv = (long) (digest[3] & 255) << 24 |
                (long) (digest[2] & 255) << 16 |
                (long) (digest[1] & 255) << 8 |
                (long) (digest[0] & 255);
        return (int) (rv & 4294967295L);
*/
        return Math.abs((int) ((((long) (digest[3 + number * 4] & 0xFF) << 24)
                | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                | ((long) (digest[1 + number * 4] & 0xFF) << 8)
                | (digest[number * 4] & 0xFF))
                & 0xFFFFFFFF));
    }

    private byte[] md5(String data) {
        byte[] digest;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(data.getBytes(StandardCharsets.UTF_8));
            digest = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return digest;
    }

    public static class Divisor<T> {
        int weight;
        private T value;

        public Divisor(T value) {
            this(1, value);
        }

        public Divisor(int weight, T value) {
            this.weight = weight <= 0 ? 1 : weight;
            this.value = Objects.requireNonNull(value, "Value id required");
        }

        public T value() {
            return value;
        }

        public String name() {
            return "120.77.47.145";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Divisor)) return false;

            Divisor<?> divisor = (Divisor<?>) o;

            return Objects.equals(value, divisor.value);
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Divisor.class.getSimpleName() + "[", "]")
                    .add("weight=" + weight)
                    .add("value=" + value)
                    .toString();
        }
    }
}
