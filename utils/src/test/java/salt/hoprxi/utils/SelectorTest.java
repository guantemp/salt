package salt.hoprxi.utils;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2023-09-18
 */
public class SelectorTest {
    @Test
    public void testSelector() {
        Selector selector = new Selector(new Selector.Divisor[0]);
        for (int i = 0; i < 5; i++) {
            selector.add(new Selector.Divisor(1, "192.168.1." + i));
        }
        //System.out.println(selector);
        String divisor = selector.select("13257685291810811");
        System.out.println(divisor);
        divisor = selector.select("13254064915397646");
        System.out.println(divisor);
        divisor = selector.select("13254064915397651");
        System.out.println(divisor);
        for (int i = 0; i < 2; i++) {
            selector.add(new Selector.Divisor(1, "192.168.1." + i));
        }
        divisor = selector.select("13254064917494814");
        System.out.println(divisor);
        divisor = selector.select("13254064917494820");
        System.out.println(divisor);
        divisor = selector.select("13254742192734115");
        System.out.println(divisor);

        divisor = selector.select("13254064915397651");
        Assert.assertEquals(divisor, "192.168.1.1");
        divisor = selector.select("13254064915397646");
        Assert.assertEquals(divisor, "192.168.1.4");
        System.out.println();

        Selector.Divisor<String> re = new Selector.Divisor("192.168.1.5");
        selector.delete(re);
        divisor = selector.select("13254064915397651");
        Assert.assertEquals(divisor, "192.168.1.1");
        divisor = selector.select("13257685291810811");
        System.out.println(divisor);

        Map<String, Integer> resMap = new HashMap<>();
        for (int i = 0; i < 1000000; i++) {
            int widgetId = ThreadLocalRandom.current().nextInt();
            String test = selector.select("&&" + widgetId);
            if (resMap.containsKey(test)) {
                resMap.put(test, resMap.get(test) + 1);
            } else {
                resMap.put(test, 1);
            }
        }
        resMap.forEach(
                (k, v) -> System.out.println("group " + k + ": " + v + "(" + v / 10000.0D + "%)")
        );
        resMap.clear();
        System.out.println();
        for (int i = 0; i < 1000000; i++) {
            String test = selector.select();
            if (resMap.containsKey(test)) {
                resMap.put(test, resMap.get(test) + 1);
            } else {
                resMap.put(test, 1);
            }
        }
        resMap.forEach(
                (k, v) -> System.out.println("select " + k + ": " + v + "(" + v / 10000.0D + "%)")
        );
        Selector.Divisor<String> add = new Selector.Divisor(1, "192.168.1.5");
        selector.add(add);
        add = new Selector.Divisor<>(1, "192.168.1.6");
        selector.add(add);
        add = new Selector.Divisor("192.168.1.7");
        selector.add(add);
        add = new Selector.Divisor("192.168.1.8");
        selector.add(add);
        resMap.clear();
        System.out.println("\nadd:");
        for (int i = 0; i < 1000000; i++) {
            String test = selector.select();
            if (resMap.containsKey(test)) {
                resMap.put(test, resMap.get(test) + 1);
            } else {
                resMap.put(test, 1);
            }
        }
        resMap.forEach(
                (k, v) -> System.out.println("select " + k + ": " + v + "(" + v / 10000.0D + "%)")
        );

        re = new Selector.Divisor("192.168.1.0");
        selector.delete(re);
        re = new Selector.Divisor("192.168.1.5");
        selector.delete(re);
        resMap.clear();
        System.out.println("\ndelete:");
        for (int i = 0; i < 1000000; i++) {
            String test = selector.select();
            if (resMap.containsKey(test)) {
                resMap.put(test, resMap.get(test) + 1);
            } else {
                resMap.put(test, 1);
            }
        }
        resMap.forEach(
                (k, v) -> System.out.println("select " + k + ": " + v + "(" + v / 10000.0D + "%)")
        );
        divisor = selector.select("3法阿8979+8526斯蒂芬");
        Assert.assertEquals(divisor, "192.168.1.1");
        divisor = selector.select("2SA双AZ发生阿萨法奥拉夫D说");
        Assert.assertEquals(divisor, "192.168.1.45");
    }

}