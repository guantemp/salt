package salt.hoprxi;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        Config config = ConfigFactory.load("resources/cache.conf");
        Config reference = ConfigFactory.load("resources/cache_reference.conf");
        config = config.withFallback(reference);
        Config temp = config.getConfig("sentinel");
        System.out.println(config.getString("redis.maxTotal"));
        System.out.println(config.getString("redis.timeout"));
        List<String> list = temp.getStringList("hosts");
        for (String s : list)
            System.out.println(s);

        config = ConfigFactory.load("resources/cache.conf");
        System.out.println(config.getString("default.l1.maxAmount"));
    }
}
