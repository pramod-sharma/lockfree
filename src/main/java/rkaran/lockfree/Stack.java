package rkaran.lockfree;

import lombok.RequiredArgsConstructor;
import net.jcip.annotations.ThreadSafe;

import java.util.EmptyStackException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of Treiber's Stack Non-Blocking
 * LockFree Concurrent Stack.
 * @param <V>
 */
@ThreadSafe
public class Stack<V> {

    @RequiredArgsConstructor()
    private static final class Node<V> {
        private final V data;
        private Node<V> next;
    }

    private final AtomicReference<Node<V>> topRef = new AtomicReference<>();

    public void push(V data) {
        Node<V> node = new Node<>(data);
        node.next = this.topRef.get();
        while(!topRef.compareAndSet(node.next, node)) {
            node.next = this.topRef.get();
        }
    }

    public boolean pop() {
        Node<V> top = this.topRef.get();
        boolean popped = false;
        while(null != top && !(popped = topRef.compareAndSet(top, top.next))) {
            top = this.topRef.get();
        }
        return popped;
    }

    public V top() {
        Node<V> top = this.topRef.get();
        if(null != top) {
            return top.data;
        } else {
            throw new IllegalStateException("Stack is empty");
        }
    }
}
