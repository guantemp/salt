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

package salt.hoprxi.cache.application;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.StringJoiner;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.1 builder 2022-04-09
 */
public class TreeTest {
    private static final Tree<String> tree = Tree.root("root");
    private static final Tree<Integer> tree1 = Tree.root(1);

    @BeforeMethod
    public void setUp() {
        tree.append("root", "1").append("1", "1-1").append("1", "1-2").append("1", "1-1").append("1", "1-2");
        tree.append("1-2", "1-2-1").append("1-2-1", "1-2-1-1").append("1-2-1", "1-2-1-2").append("1-2-1", "1-2-1-3").append("1-2", "1-2-2");
        tree.append("1", "1-3");
        tree.append("root", "2").append("2", "2-1");
        tree.append("root", "3").append("3", "3-1").append("3-1", "3-1-1").append("3", "3-2");
        tree.append("root", "4").append("4", "4-2", "4-4").append("4", "4-1", "4-3");
    }

    @Test(priority = 1)
    public void testAppend() {
        tree.append("3-1-1", "3-1-1-1").append("3-1-1", "3-1-1-2");
    }

    @Test(priority = 2)
    public void testRoot() {
        Assert.assertEquals("root", tree.root());
        Tree<User> userTree = new Tree<>(new User(1, "root", "aszfd"));
        User root = userTree.root();
        userTree.append(root, new User(2, "顺风车", "的沙发大厦"), new User(3, "沙发上", "企鹅万人"));
        userTree.update(new User(2, "顺风车", "哈哈哈哈哈哈艾达王问题了万千哈哈"));
    }

    @Test(priority = 2)
    public void testChildren() {
        Assert.assertEquals(4, tree.children("root").length);
        Assert.assertEquals(2, tree.children("1-2").length);
        Assert.assertEquals("3", tree.children("root")[2]);
        Assert.assertEquals(2, tree.children("3").length);
        Assert.assertEquals(1, tree.children("2").length);
        Assert.assertEquals(3, tree.children("1").length);
        Assert.assertEquals("1-2-1-2", tree.children("1-2-1")[1]);
        Assert.assertEquals(0, tree.children(null).length);

        System.out.println("1-2 children:");
        for (String s : tree.children("1-2"))
            System.out.println(s);
    }

    @Test(priority = 2)
    public void testDescendants() {
        Assert.assertEquals(22, tree.descendants("root").length);
        Assert.assertEquals(5, tree.descendants("1-2").length);
        Assert.assertEquals(5, tree.descendants("3").length);
        Assert.assertEquals(1, tree.descendants("2").length);
        Assert.assertEquals(8, tree.descendants("1").length);
        Assert.assertEquals("1-1", tree.descendants("1")[0]);
        Assert.assertEquals("1-2-1-2", tree.descendants("1-2")[2]);

        System.out.println("1 descendants:");
        for (String s : tree.descendants("1"))
            System.out.println(s);
        System.out.println("4 descendants:");
        for (String s : tree.descendants("4"))
            System.out.println(s);
        System.out.println("1 bfsorder:");
        for (String s : tree.descendantsOfBFS("1"))
            System.out.println(s);
        System.out.println("root descendants:");
        for (String s : tree.descendants())
            System.out.println(s);
    }

    @Test(priority = 2)
    public void testSiblings() {
        Assert.assertEquals(0, tree.siblings("root").length);
        Assert.assertEquals(3, tree.siblings("1").length);
        Assert.assertEquals("2", tree.siblings("1")[0]);
        Assert.assertEquals(1, tree.siblings("1-2-1").length);
        Assert.assertEquals(3, tree.siblings("2").length);
        Assert.assertEquals(1, tree.siblings("1-2-2").length);
        Assert.assertEquals(2, tree.siblings("1-2-1-1").length);
        Assert.assertEquals(0, tree.siblings(null).length);
    }

    @Test(priority = 2)
    public void testIsChild() {
        Assert.assertTrue(tree.isChild("1", "1-2"));
        Assert.assertFalse(tree.isChild("root", "1-2-1"));
        Assert.assertTrue(tree.isChild("root", "2"));
        Assert.assertFalse(tree.isChild("root", "2-1"));
        Assert.assertFalse(tree.isChild("2", "2-11"));
        Assert.assertTrue(tree.isChild("2", "2-1"));
        Assert.assertFalse(tree.isChild("4", "2-1"));
        Assert.assertFalse(tree.isChild(null, "2-1"));
        Assert.assertFalse(tree.isChild("root", null));
    }

    @Test(priority = 2)
    public void testIsDescendant() {
        Assert.assertTrue(tree.isDescendant("root", "1-2-1"));
        Assert.assertTrue(tree.isDescendant("1", "1-2-1-3"));
        Assert.assertTrue(tree.isDescendant("root", "1-2-1-3"));
        Assert.assertFalse(tree.isDescendant("root", "1-2-1-31"));
        Assert.assertFalse(tree.isDescendant("2", "1-2-1-3"));
        Assert.assertTrue(tree.isDescendant("2", "2-1"));
        Assert.assertFalse(tree.isDescendant("4", "2-1"));
        Assert.assertFalse(tree.isDescendant(null, "2-1"));
        Assert.assertFalse(tree.isDescendant("root", null));
    }

    @Test(priority = 2)
    public void testDepth() {
        Assert.assertEquals(0, tree.depth("5897"));
        Assert.assertEquals(0, tree.depth(null));
        Assert.assertEquals(4, tree.depth("1-2-1"));
        Assert.assertEquals(2, tree.depth("3"));
        //Assert.assertEquals(2, tree.depth("2"));
        Assert.assertEquals(3, tree.depth("3-2"));
        Assert.assertEquals(1, tree.depth());
    }

    @Test(priority = 2)
    public void testIsLeaf() {
        Assert.assertFalse(tree.isLeaf("root"));
        Assert.assertTrue(tree.isLeaf("1-2-1-2"));
        Assert.assertTrue(tree.isLeaf("2-1"));
        Assert.assertFalse(tree.isLeaf("2"));
        Assert.assertFalse(tree.isLeaf("3-1"));
        Assert.assertFalse(tree.isLeaf("25434"));
        Assert.assertFalse(tree.isLeaf(null));
    }

    @Test(priority = 2)
    public void testKeys() {
        //Assert.assertEquals(18, tree.keys().length);
    }

    @Test(priority = 2)
    public void testPath() {
        Assert.assertEquals(5, tree.path("1-2-1-1").length);
        Assert.assertEquals("1-2-1-1", tree.path("1-2-1-1")[0]);
        Assert.assertEquals("1-2", tree.path("1-2-1-1")[2]);
        Assert.assertEquals("root", tree.path("1-2-1-1")[4]);
        Assert.assertEquals("root", tree.path("root")[0]);
        Assert.assertEquals(0, tree.path("256").length);
        Assert.assertEquals(0, tree.path(null).length);

        System.out.println("path:");
        for (String s : tree.path("1-2-1-3"))
            System.out.println(s);
    }

    @Test(priority = 2)
    public void testContain() {
        Assert.assertFalse(tree.contain("2345"));
        Assert.assertTrue(tree.contain("2"));
        Assert.assertTrue(tree.contain("1-2-1"));
        Assert.assertFalse(tree.contain("1-4"));
    }

    @Test(priority = 2)
    public void testParent() {
        Assert.assertEquals("root", tree.parent("2"));
        Assert.assertEquals("root", tree.parent("1"));
        Assert.assertEquals("1-2", tree.parent("1-2-1"));
        Assert.assertEquals("1", tree.parent("1-2"));
        Assert.assertEquals("2", tree.parent("2-1"));
        Assert.assertNull(tree.parent("root"));
        Assert.assertNull(tree.parent("2345"));
        System.out.println("parent:");
        System.out.println(tree.parent("root"));
        System.out.println(tree.parent("2345"));
    }

    @Test(priority = 3)
    public void tesMove() {
        tree.move("3-1-1", "3");
        Assert.assertEquals("3-1-1", tree.children("3")[0]);
        Assert.assertEquals("3-1", tree.children("3")[1]);
        tree.move("3", "3-1-1").move("root", "1-2");
        Assert.assertEquals("3-1-1", tree.children("3")[0]);
        Assert.assertEquals("3-1", tree.children("3")[1]);

        Assert.assertEquals("1", tree.children("root")[0]);
        Assert.assertEquals("1-2", tree.children("1")[1]);
    }

    @Test(priority = 4)
    public void testRemove() {
        Assert.assertEquals(3, tree.descendants("1-2-1").length);
        tree.remove("1-2-1").remove("1-2-1-1");
        Assert.assertEquals(0, tree.descendants("1-2-1").length);
        Assert.assertEquals(1, tree.descendants("1-2").length);
        Assert.assertFalse(tree.contain("1-2-1"));
    }

    @Test(priority = 5)
    public void testClear() {
        Assert.assertTrue(tree.contain("1-2"));
        Assert.assertTrue(tree.contain("root"));
        tree.clear();
        //Assert.assertEquals(0, tree.keys().length);
        Assert.assertFalse(tree.contain("root"));
    }

    private static class User {
        int id;
        String name;
        String password;

        public User(int id, String name, String password) {
            this.id = id;
            this.name = name;
            this.password = password;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof User)) return false;

            User user = (User) o;

            return id == user.id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", User.class.getSimpleName() + "[", "]")
                    .add("id=" + id)
                    .add("name='" + name + "'")
                    .add("password='" + password + "'")
                    .toString();
        }
    }
}