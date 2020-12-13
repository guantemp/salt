package salt.hoprxi.crypto;

import org.testng.Assert;
import org.testng.annotations.Test;
import salt.hoprxi.crypto.algorithms.Scrypt;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.1 2020-12-13
 * @since JDK8.0
 */
public class HashServiceTest {
    @Test
    public void scryptCheck() {
        HashService hash = new ScryptHash();
        Assert.assertTrue(hash.check("邯郸市而我认为谁当皇上是沃尔沃时候糖果乐园", "$s0$f0801$7x2rRvysEA17rcrwslpq7g==$Q7cqWN+7dgE44bH0oKxvhALm4MHezJwhQo2/ijdB9WA="));
        System.out.println(Scrypt.verify("邯郸市而我认为谁当皇上是沃尔沃时候糖果乐园", "$s0$e0801$qDtkzfxYnBcr6A20Kbsk2A==$DtM/HgJhWcVa5gMVPgTOVJyl+1XQVs7MXdL6ckq/07A="));
        System.out.println(Scrypt.scrypt("yu"));
    }

    @Test
    public void bcryptEncryptAndCheck() {
        HashService hash = new BcryptHash();
        System.out.println(hash.hash("邯郸市而我认为谁当皇上是沃尔沃时候糖果乐园"));
        Assert.assertTrue(hash.check("邯郸市而我认为谁当皇上是沃尔沃时候糖果乐园", "$2a$12$1Op0r.a9Fci9WUVfRWDlFeL1aOpB097q23iuPabS2I0UR2HcZAlC."));
    }

    @Test
    public void pbhkdf2EncryptAndCheck() {
        HashService hash = new Pbhkdf2Hash();
        System.out.println(hash.hash("邯郸市而我认为谁当皇上是沃尔沃时候糖果乐园"));
        Assert.assertTrue(hash.check("邯郸市而我认为谁当皇上是沃尔沃时候糖果乐园", "888bc1892a25f67b8ffdf026c4275e19:9f178340e577395ec8a158f96c6e18d05e147d340e6a87e9938dfc0eb7c852e0"));
    }

    @Test
    public void SM3EncryptAndCheck() {
        HashService hash = new SM3Hash();
        System.out.println(hash.hash("邯郸市而我认为谁当皇上是沃尔沃时候糖果乐园"));
        Assert.assertTrue(hash.check("邯郸市而我认为谁当皇上是沃尔沃时候糖果乐园", "681f9bb3ae34a6e7616c93b29b63e5554c9e8b253ffc2987d53ffcdf2824fb9b"));
    }
}