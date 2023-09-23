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

import salt.hoprxi.cache.Cache;
import salt.hoprxi.cache.CacheFactory;
import salt.hoprxi.to.HumpToUnderline;

import java.lang.reflect.Array;
import java.util.*;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.0.3 builder 2022-10-25
 */
public class Tree<T> {
    private final Cache<T, Node<T>> cache;
    private final T root;

    public Tree(T root) {
        this.root = Objects.requireNonNull(root, "root required");
        cache = CacheFactory.build(HumpToUnderline.format(root.getClass().getSimpleName()));
        //System.out.println(HumpToUnderline.format(root.getClass().getSimpleName()));
        cache.put(root, Node.root(root));
    }

    public static <T> Tree<T> root(T root) {
        return new Tree<>(root);
    }

    public synchronized Tree<T> append(T parent, T child) {
        Objects.requireNonNull(parent, "parent required");
        Objects.requireNonNull(child, "child required");
        Node<T> parentNode = cache.get(parent);
        if (parentNode == null)
            throw new InvalidParentNodeException("Not find parent node");
        Node<T> childNode = cache.get(child);
        if (childNode == null) {
            append(parentNode, child);
            cache.put(child, Node.parent(parent, child));
        }
        return this;
    }

    public synchronized Tree<T> append(T parent, T... children) {
        Objects.requireNonNull(parent, "parent required");
        Objects.requireNonNull(children, "children required");
        Node<T> parentNode = cache.get(parent);
        if (parentNode == null)
            throw new InvalidParentNodeException("Invalid parent node");
        T firstChild = null;
        Node<T> preChildNode = null;
        for (T child : children) {
            Node<T> childNode = cache.get(child);
            if (childNode == null) {
                childNode = Node.parent(parent, child);
                if (firstChild == null)
                    firstChild = child;
                if (preChildNode != null)
                    preChildNode.setRightSibling(child);
                preChildNode = childNode;
                cache.put(child, childNode);
            }
        }
        append(parentNode, firstChild);
        return this;
    }

    private void append(Node<T> parentNode, T firstChild) {
        T leftChild = parentNode.leftChild();
        if (leftChild == null) {
            parentNode.setLeftChild(firstChild);
        } else {
            T prefix = leftChild;
            T last = leftChild;
            while (last != null) {
                prefix = last;
                last = cache.get(prefix).rightSibling();
            }
            cache.get(prefix).setRightSibling(firstChild);
        }
    }

    public void update(T key) {
        Objects.requireNonNull(key);
        Node<T> node = cache.get(key);
        node = node.update(key);
        cache.put(key, node);
    }

    public synchronized Tree<T> move(T from, T to) {
        Objects.requireNonNull(from, "from required");
        Objects.requireNonNull(to, "to required");
        Node<T> toNode = cache.get(to);
        Node<T> fromNode = cache.get(from);
        if (fromNode != null && !fromNode.isRoot() && toNode != null && !fromNode.parent().equals(to) && !isDescendant(from, to)) {
            disconnectOriginalLocation(from);
            T left = toNode.leftChild();
            toNode.setLeftChild(from);
            fromNode.setRightSibling(left);
        }
        return this;
    }

    public synchronized Tree<T> remove(T key) {
        Objects.requireNonNull(key, "key required");
        if (key.equals(root)) {
            cache.clear();
            return Tree.root(key);
        }
        for (T temp : descendants(key)) {
            cache.evict(temp);
        }
        disconnectOriginalLocation(key);
        cache.evict(key);
        return this;
    }


    private void disconnectOriginalLocation(T key) {
        Node<T> selfNode = cache.get(key);
        if (selfNode == null)
            return;
        Node<T> parentNode = cache.get(selfNode.parent());
        T left = parentNode.leftChild();
        if (left.equals(key)) {
            parentNode.setLeftChild(selfNode.rightSibling());
        } else {
            T sibling = selfNode.rightSibling();
            Node<T> siblingNode = cache.get(sibling);
            Node<T> preNode = siblingNode;
            while (sibling != null && !sibling.equals(key)) {
                siblingNode = cache.get(sibling);
                preNode = siblingNode;
                sibling = siblingNode.rightSibling();
            }
            preNode.setRightSibling(cache.get(sibling).rightSibling());
        }
    }

    public T root() {
        return root;
    }

    @SuppressWarnings({"unchecked", "hiding"})
    public T[] children(T key) {
        Node<T> parentNode = cache.get(key);
        if (parentNode != null && !parentNode.isLeaf()) {
            List<T> list = new ArrayList<>();
            T child = parentNode.leftChild();
            while (child != null) {
                list.add(child);
                child = cache.get(child).rightSibling();
            }
            return list.toArray((T[]) Array.newInstance(key.getClass(), 0));
        }
        return (T[]) Array.newInstance(root.getClass(), 0);
    }

    /**
     * @param target
     * @param child
     * @return
     */
    public boolean isChild(T target, T child) {
        if (target == null || cache.get(target) == null || child == null)
            return false;
        T left = cache.get(target).leftChild();
        while (left != null) {
            if (left.equals(child))
                return true;
            left = cache.get(left).rightSibling();
        }
        return false;
    }

    @SuppressWarnings({"unchecked", "hiding"})
    public T[] siblings(T key) {
        Node<T> keyNode = cache.get(key);
        if (keyNode == null || keyNode.isRoot())
            return (T[]) Array.newInstance(root.getClass(), 0);
        List<T> list = new ArrayList<>();
        T left = cache.get(keyNode.parent()).leftChild();
        while (left != null) {
            if (!left.equals(key))
                list.add(left);
            left = cache.get(left).rightSibling();
        }
        return list.toArray((T[]) Array.newInstance(root.getClass(), 0));
    }

    public T[] descendants() {
        return descendants(root);
    }

    //深度优先搜索（DFS）
    @SuppressWarnings({"unchecked", "hiding"})
    public T[] descendants(T parent) {
        Node<T> parentNode = cache.get(parent);
        if (parentNode != null && !parentNode.isLeaf()) {
            List<T> list = new ArrayList<>();
            recursion(list, parentNode);
            return list.toArray((T[]) Array.newInstance(parent.getClass(), 0));
        }
        return (T[]) Array.newInstance(parent.getClass(), 0);
    }

    /**
     * @param list       recursion will append
     * @param parentNode parent node
     */
    private void recursion(List<T> list, Node<T> parentNode) {
        T left = parentNode.leftChild();
        while (left != null) {
            list.add(left);
            Node<T> leftNode = cache.get(left);
            if (leftNode.leftChild() != null)
                recursion(list, leftNode);
            left = leftNode.rightSibling();
        }
    }

    //广度优先搜索
    @SuppressWarnings({"unchecked", "hiding"})
    public T[] descendantsOfBFS(T key) {
        Node<T> node = cache.get(key);
        if (node != null && !node.isLeaf()) {
            List<T> list = new ArrayList<>();
            node = cache.get(node.leftChild());
            recursionOfBFS(list, node);
            return list.toArray((T[]) Array.newInstance(root.getClass(), 0));
        }
        return (T[]) Array.newInstance(root.getClass(), 0);
    }

    private void recursionOfBFS(List<T> list, Node<T> node) {
        T right = node.rightSibling();
        while (right != null) {
            list.add(right);
            Node<T> rightNode = cache.get(right);
            if (rightNode.rightSibling() != null)
                recursionOfBFS(list, rightNode);
            right = rightNode.leftChild();
        }
    }

    /**
     * @param target
     * @param descendant
     * @return true if target has descendant()
     */
    public boolean isDescendant(T target, T descendant) {
        if (target == null || cache.get(target) == null || descendant == null) {
            return false;
        } else if (target.equals(descendant)) {
            return true;
        } else {
            Node<T> temp = cache.get(target);
            boolean a = isDescendant(temp.leftChild(), descendant);
            boolean b = isDescendant(temp.rightSibling(), descendant);
            return a || b;
        }
    }

    @SuppressWarnings({"unchecked", "hiding"})
    public T[] path(T key) {
        Node<T> node = cache.get(key);
        if (node == null) {
            return (T[]) Array.newInstance(root.getClass(), 0);
        } else {
            List<T> list = new ArrayList<>();
            list.add(node.key());
            while (!node.isRoot()) {
                node = cache.get(node.parent());
                list.add(node.key());
            }
            return list.toArray((T[]) Array.newInstance(key.getClass(), 0));
        }
    }


    public int depth() {
        return depth(root);
    }

    public int depth(T key) {
        Node<T> node = cache.get(key);
        if (node == null) {
            return 0;
        } else {
            int depth = 1;
            while (!node.isRoot()) {
                depth++;
                node = cache.get(node.parent());
            }
            return depth;
        }
        /*
        if (key == null || cache.get(key) == null) {
            return 0;
        } else {
            Node<T> temp = cache.get(key);
            int a = depth(temp.leftChild()) + 1;
            temp = cache.get(temp.leftChild());
            int b = 0;
            if (temp != null)
                b = depth(temp.rightSibling());
            return Math.max(a, b);
        }
         */
    }

    public boolean isLeaf(T key) {
        Optional<Node<T>> node = Optional.ofNullable(cache.get(key));
        return node.map(k -> Boolean.TRUE).orElse(Boolean.FALSE) ? node.map(Node::leftChild).map(c -> Boolean.FALSE).orElse(Boolean.TRUE) : false;
    }

    @SuppressWarnings({"unchecked", "hiding"})
    public T[] keys() {
        return (T[]) Array.newInstance(root.getClass(), 0);
        //return cache.keySet().toArray((T[]) Array.newInstance(root.key().getClass(), cache.size()));
    }

    public boolean contain(T key) {
        return cache.get(key) != null;
    }

    public T value(T key) {
        return cache.get(key).key();
    }

    public Tree<T> narrow(T key) {
        return null;
    }

    public T parent(T key) {
        return Optional.ofNullable(cache.get(key)).map(Node::parent).orElse(null);
    }

    public synchronized void clear() {
        cache.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tree)) return false;

        Tree<?> tree = (Tree<?>) o;

        return Objects.equals(root, tree.root);
    }

    @Override
    public int hashCode() {
        return root != null ? root.hashCode() : 0;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Tree.class.getSimpleName() + "[", "]")
                .add("cache=" + cache)
                .add("root=" + root)
                .toString();
    }
}
