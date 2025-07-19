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

package salt.hoprxi.cache.application;

import java.util.Objects;
import java.util.StringJoiner;

/***
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuang</a>
 * @since JDK8.0
 * @version 0.1.2 builder 2022-04-05
 */
public class Node<T> {
    private final T key;
    private T parent;
    private T leftChild;
    private T rightSibling;
    private boolean root;

    public Node(T key) {
        this.key = Objects.requireNonNull(key, "key required");
    }

    private Node(T key, T parent, T leftChild, T rightSibling, boolean root) {
        this.key = Objects.requireNonNull(key, "key required");
        setParent(parent);
        setLeftChild(leftChild);
        setRightSibling(rightSibling());
        this.root = root;
    }

    public static <T> Node<T> parent(T parent, T key) {
        Node<T> temp = new Node<>(key);
        temp.setParent(parent);
        return temp;
    }

    public static <T> Node<T> root(T key) {
        Node<T> root = new Node<>(key);
        root.root = true;
        return root;
    }

    public Node<T> update(T key) {
        return new Node<>(key, parent, leftChild, rightSibling, root);
    }

    public void append(Node child) {

    }

    public void setParent(T parent) {
        this.parent = Objects.requireNonNull(parent, "parent required");
    }

    public void setLeftChild(T leftChild) {
        this.leftChild = leftChild;
    }

    public void setRightSibling(T rightSibling) {
        this.rightSibling = rightSibling;
    }

    public T key() {
        return key;
    }

    public T parent() {
        return parent;
    }

    public T leftChild() {
        return leftChild;
    }

    public T rightSibling() {
        return rightSibling;
    }

    public boolean isRoot() {
        return root;
    }

    public boolean isLeaf() {
        return leftChild == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;

        Node<?> node = (Node<?>) o;

        return Objects.equals(key, node.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Node.class.getSimpleName() + "[", "]")
                .add("key=" + key)
                .add("parent=" + parent)
                .add("leftChild=" + leftChild)
                .add("rightSibling=" + rightSibling)
                .add("root=" + root)
                .toString();
    }
}
