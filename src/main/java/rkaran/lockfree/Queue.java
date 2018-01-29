package rkaran.lockfree;

import lombok.RequiredArgsConstructor;
import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of "Michael & Scott"'s Non-Blocking
 * LockFree Concurrent Queue.
 * @param <V>
 */
@ThreadSafe
public class Queue<V> {

    @RequiredArgsConstructor
    private static final class Node<V> {
        private final V data;
        private AtomicReference<Node<V>> next = new AtomicReference<>(null);
    }

    private AtomicReference<Node<V>> sentinelRef = new AtomicReference<>(new Node<>(null));
    private AtomicReference<Node<V>> tailRef = new AtomicReference<>(this.sentinelRef.get());

    public void enque(V data) {
        Node<V> node = new Node<>(data);
        while(true) {
            Node<V> tail = this.tailRef.get();
            Node<V> next = tail.next.get();
            if(null != next) {
                this.tailRef.compareAndSet(tail, next);
            } else {
                if(tail.next.compareAndSet(null, node)) {
                    this.tailRef.compareAndSet(tail, node);
                    break;
                }
            }
        }
    }

    public boolean deque() {
        while(true) {
            Node<V> sentinel = this.sentinelRef.get();
            Node<V> tail = this.tailRef.get();
            if(sentinel != tail) {
                if(this.sentinelRef.compareAndSet(sentinel, sentinel.next.get())) {
                    return true;
                }
            } else {
                Node<V> next = tail.next.get();
                if(null != next) {
                    this.tailRef.compareAndSet(tail, next);
                } else {
                    return false;
                }
            }
        }
    }

    public V front() {
        Node<V> sentinel = this.sentinelRef.get();
        Node<V> head = sentinel.next.get();
        if(null != head) {
            return head.data;
        } else {
            throw new IllegalStateException("Queue is empty");
        }
    }
}
