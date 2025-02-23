package org.example;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * @author sparkle6979l
 * @version 1.0
 * @data 2025/2/22 21:03
 */
public class MyLock {
    Boolean fair = false;
    AtomicBoolean flag = new AtomicBoolean(false);
    Thread owner = null;
    AtomicReference<Node> head = new AtomicReference<>(new Node());
    AtomicReference<Node> tail = new AtomicReference<>(head.get());
    public MyLock(){}

    public MyLock(Boolean fair) {
        this.fair = fair;
    }

    public void lock() {

        if (!fair && flag.compareAndSet(false, true)){
            owner = Thread.currentThread();
            return;
        }
        Node current = new Node();
        current.thread = Thread.currentThread();

        while (true){
            Node currentTail = tail.get();
            // 不断尝试将尾节点替换为当前节点
            if (tail.compareAndSet(currentTail, current)) {
                System.out.println(Thread.currentThread().getName() + "加入了链表中");
                current.pre = currentTail;
                currentTail.next = current;
                break;
            }
        }

        while (true){
            if(current.pre == head.get() && flag.compareAndSet(false, true)){
                owner = Thread.currentThread();

                head.set(current);
                current.pre.next = null;
                current.pre = null;

                System.out.println(Thread.currentThread().getName() + "被唤醒后，拿到锁");
                return;
            }

            LockSupport.park();
        }


    }


    public void unlock() {
        if(owner != Thread.currentThread()){
            throw new IllegalStateException("当前线程并没有锁，不能解锁");
        }
        Node node = head.get().next;
//        System.out.println("node : " + node);
        // 只有持有锁的线程才会执行该逻辑，故不需要CAS
        flag.set(false);
        if(node != null){
            System.out.println(Thread.currentThread().getName() + "唤醒了" + node.thread.getName());
            LockSupport.unpark(node.thread);
        }
    }

    class Node {
        Node pre;
        Node next;
        Thread thread;
    }
}
