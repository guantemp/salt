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

package salt.hoprxi.crypto;

import org.testng.Assert;
import org.testng.annotations.Test;
import salt.hoprxi.crypto.hash.Argon2Hash;
import salt.hoprxi.crypto.hash.Pbhkdf2Hash;
import salt.hoprxi.crypto.hash.SM3Hash;
import salt.hoprxi.crypto.hash.ScryptHash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author <a href="www.hoprxi.com/author/guan xianghuang">guan xiangHuan</a>
 * @version 0.0.1 2020-12-13
 * @since JDK8.0
 */
public class HashServiceTest {
    @Test
    public void scryptCheck() {
        HashService hash = new ScryptHash();
        System.out.println(hash.hash("爱上了对方无穷乐趣而为的故事回答说住宿费"));
        Assert.assertTrue(hash.matches("爱上了对方无穷乐趣而为的故事回答说住宿费", "$s0$100801$FY5rkrgAnW4FJUQtnx+kUA==$fPluqcREnuDr7jvgZB4Ywv5ePzHutUKlF/gsbOI/+Co="));
    }

    @Test
    public void argon2Check() {
        HashService hash = new Argon2Hash();
        System.out.println(hash.hash("爱上了对方无穷乐趣而为的故事回答说住宿费"));
        System.out.println(hash.matches("爱上了对方无穷乐趣而为的故事回答说住宿费", "$argon2id$v=19$m=16384,t=2,p=1$HyOKD+jvKJQEba5eaFY4qw==$f3my5HsDDpdkR3IgUgKudIwNFVmsqLA/hYWwP17mKYQ="));
        Assert.assertTrue(hash.matches("爱上了对方无穷乐趣而为的故事回答说住宿费", "$argon2id$v=19$m=16384,t=2,p=1$27Qs2LC2njKqgWQTYiaaLg==$/DXE/dMLgMv4Icy9Nbf3omHyqtjfoQX/1KQm7OsukE8="));
    }

    @Test(invocationCount = 2, threadPoolSize = 2)
    public void sha512() throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
        byte[] message = messageDigest.digest("爱上了对方无穷乐趣而为的故事回答说住宿费".getBytes(StandardCharsets.UTF_8));
        System.out.println(Base64.getEncoder().encodeToString(message));
        message = messageDigest.digest("邯郸市而我认为谁当皇上是沃尔沃神大衮的身上萨尔浒撒旦法时候糖果乐园".getBytes(StandardCharsets.UTF_8));
        System.out.println(Base64.getEncoder().encodeToString(message));
    }

    @Deprecated
    @Test
    public void bcryptEncryptAndCheck() throws NoSuchAlgorithmException {
        HashService hash = new BcryptHash();
        System.out.println(hash.hash("沙发多个辅导员发动反攻"));
//        byte[] pre = BCrypt.passwordToByteArray("沙发多个辅导员发动反攻".toCharArray());
//        byte[] salt = new byte[16];
//        SecureRandom secureRandom = SecureRandom.getInstanceStrong();
//        secureRandom.nextBytes(salt);
//        byte[] bytes = org.bouncycastle.crypto.generators.BCrypt.generate("沙发多32452个辅导员发sdfds动反攻".getBytes(StandardCharsets.UTF_8), salt, 4);
//        System.out.println(Base64.getEncoder().encodeToString(bytes));
        System.out.println("邯郸市而我认为谁当皇上是沃社团的风格梵蒂冈的风格尔沃时候糖果乐园阿凡达上海市而是特务社团的深V阿凡达权威去 问他问题的人会更好电话好不好耳温枪桶 晴儿提4受到辐射范围尔维特方式玩起来3打工的".length());
        Assert.assertTrue(hash.matches("沙发多个辅导员发动反攻", "$2a$10$yx2tiRenN3x/Povb2kWGwOfTjX5KTGpEU/2ys5RnB262riOaA6w5S"));
    }

    @Test
    public void pbhkdf2EncryptAndCheck() {
        HashService hash = new Pbhkdf2Hash();
        System.out.println(hash.hash("爱上了对方无穷乐趣而为的故事回答说住宿费"));
        Assert.assertTrue(hash.matches("爱上了对方无穷乐趣而为的故事回答说住宿费", "217c26ff5070cf39c173677fa59f5f8362009ccaee69c843a6d6ec39282baa09:e3f8eaa8ac63a902076dbe034d1bf3c71eac279f1f1a6e7cb27a67bb5b4e6b74"));
    }

    @Test(priority = 1, invocationCount = 2)
    public void SM3EncryptAndCheck() {
        HashService hash = new SM3Hash();
        System.out.println(hash.hash("Qwe123465"));
        System.out.println(hash.hash("爱上了对方无穷乐趣而为的故事回答说住宿费"));
        System.out.println(hash.hash("邯郸市而我认为谁当皇上是沃尔沃神大衮的身上萨撒旦法沃尔沃网起来的时候涉案的话深度高峰时段递归上海是个是电费啥还是打的时候阿斯达斯大实话尔浒撒旦法时候糖果乐园"));
        Assert.assertTrue(hash.matches("爱上了对方无穷乐趣而为的故事回答说住宿费", "81d4d3b338b10a5d1889e195bebe04ecaed2951a2e59242182ae2ca86769533a"));
    }
}