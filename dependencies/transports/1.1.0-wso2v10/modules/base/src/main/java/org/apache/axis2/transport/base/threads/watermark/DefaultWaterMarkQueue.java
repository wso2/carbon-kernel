/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.base.threads.watermark;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A Default implementation for WaterMarkQueue interface. The implementation uses an
 * {@link ArrayBlockingQueue} up to water mark. Then it uses a {@link LinkedBlockingQueue} or
 * ArrayBlocking queue from the water mark point. The LinkedBlockingQueue is used if a queue
 * size is specified other than the waterMark.
 *
 * @param <T>
 */
public class DefaultWaterMarkQueue<T> implements WaterMarkQueue<T> {

    private volatile ArrayBlockingQueue<T> waterMarkQueue;

    private volatile Queue<T> afterWaterMarkQueue;

    private Lock lock = new ReentrantLock();

    /**
     * Create a {@link WaterMarkQueue} with a waterMark. The queue will first fill up
     * to waterMark. These items will be inserted in to an {@link ArrayBlockingQueue}.
     * After this an {@link LinkedBlockingQueue} will be used without a bound.
     *
     * @param waterMark the waterMark of the queue
     */
    public DefaultWaterMarkQueue(int waterMark) {
        afterWaterMarkQueue = new LinkedBlockingQueue<T>();

        waterMarkQueue = new ArrayBlockingQueue<T>(waterMark);
    }

    /**
     * Create a {@link WaterMarkQueue} with a waterMark. The queue will first fill up
     * to waterMark. These items will be inserted in to an {@link ArrayBlockingQueue}.
     * After this an {@link LinkedBlockingQueue} will be used with capacity
     * <code>size - waterMark.</code>
     *
     * @param waterMark the waterMark of the queue
     * @param size the size of the queue
     */
    public DefaultWaterMarkQueue(int waterMark, int size) {
        if (waterMark <= size) {
            afterWaterMarkQueue = new ArrayBlockingQueue<T>(size - waterMark);
        } else {
            throw new IllegalArgumentException("Size should be equal or greater than water mark");
        }

        waterMarkQueue = new ArrayBlockingQueue<T>(waterMark);
    }

    public boolean add(T t) {
        return waterMarkQueue.add(t);

    }

    public boolean offer(T t) {
        return waterMarkQueue.offer(t);
    }

    public T remove() {
        T t = waterMarkQueue.remove();
        tryMoveTasks();
        return t;
    }

    public T poll() {
        T t = waterMarkQueue.poll();
        tryMoveTasks();
        return t;
    }

    public T element() {
        return waterMarkQueue.element();
    }

    public T peek() {
        return waterMarkQueue.peek();
    }

    public void put(T t) throws InterruptedException {
        waterMarkQueue.put(t);
    }

    public boolean offer(T t, long l, TimeUnit timeUnit) throws InterruptedException {
        return waterMarkQueue.offer(t, l, timeUnit);
    }

    public T take() throws InterruptedException {
        T t = waterMarkQueue.take();
        tryMoveTasks();
        return t;
    }

    public T poll(long l, TimeUnit timeUnit) throws InterruptedException {
        T t = waterMarkQueue.poll(l, timeUnit);
        tryMoveTasks();
        return t;
    }

    public int remainingCapacity() {
        return waterMarkQueue.remainingCapacity();
    }

    public boolean remove(Object o) {
        boolean b = waterMarkQueue.remove(o);
        tryMoveTasks();
        return b;
    }

    public boolean containsAll(Collection<?> objects) {
        return waterMarkQueue.containsAll(objects);
    }

    public boolean addAll(Collection<? extends T> ts) {
        return waterMarkQueue.addAll(ts);
    }

    public boolean removeAll(Collection<?> objects) {
        boolean b = waterMarkQueue.removeAll(objects);
        tryMoveTasks();

        return b;
    }

    public boolean retainAll(Collection<?> objects) {
        return waterMarkQueue.retainAll(objects);
    }

    public void clear() {
        waterMarkQueue.clear();
        afterWaterMarkQueue.clear();
    }

    public int size() {
        return waterMarkQueue.size() + afterWaterMarkQueue.size();
    }

    public boolean isEmpty() {
        tryMoveTasks();
        return waterMarkQueue.isEmpty();
    }

    private void tryMoveTasks() {
        if (afterWaterMarkQueue.size() > 0) {
            lock.lock();
            try {
                while (afterWaterMarkQueue.size() > 0) {
                    T w = afterWaterMarkQueue.poll();
                    boolean offer = waterMarkQueue.offer(w);
                    if (!offer) {
                        afterWaterMarkQueue.offer(w);
                        break;
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public boolean contains(Object o) {
        return waterMarkQueue.contains(o) || afterWaterMarkQueue.contains(o);
    }

    public Iterator<T> iterator() {
        return new IteratorImpl();
    }

    public Object[] toArray() {
        return waterMarkQueue.toArray();
    }

    public <T> T[] toArray(T[] ts) {
        T[] waterMarkArray = waterMarkQueue.toArray(ts);
        T[] afterWaterMarkArray = afterWaterMarkQueue.toArray(ts);

        final int alen = waterMarkArray.length;
        final int blen = afterWaterMarkArray.length;
        if (alen == 0) {
            return afterWaterMarkArray;
        }

        if (blen == 0) {
            return waterMarkArray;
        }

        final T[] result = (T[]) java.lang.reflect.Array.
                newInstance(waterMarkArray.getClass().getComponentType(), alen + blen);
        System.arraycopy(waterMarkArray, 0, result, 0, alen);
        System.arraycopy(afterWaterMarkArray, 0, result, alen, blen);
        return result;
    }

    public int drainTo(Collection<? super T> objects) {
        int n = waterMarkQueue.drainTo(objects);
        tryMoveTasks();

        return n;
    }

    public int drainTo(Collection<? super T> objects, int i) {
        int n = waterMarkQueue.drainTo(objects, i);
        tryMoveTasks();
        return n;
    }

    public boolean offerAfter(T t) {
        lock.lock();
        try {
            return afterWaterMarkQueue.offer(t);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Iterator for DefaultWaterMarkQueue
     */
    private class IteratorImpl implements Iterator<T> {
        Iterator<T> waterMarkIterator = null;

        Iterator<T> afterWaterMarkIterator = null;

        boolean waterMarkQueueDone = false;

        private IteratorImpl() {
            waterMarkIterator = waterMarkQueue.iterator();
            afterWaterMarkIterator = afterWaterMarkQueue.iterator();

            waterMarkQueueDone = false;
        }

        public boolean hasNext() {
            return waterMarkIterator.hasNext() || afterWaterMarkIterator.hasNext();
        }

        public T next() {
            lock.lock();
            try {
                if (waterMarkIterator.hasNext()) {
                    return waterMarkIterator.next();
                } else {
                    waterMarkQueueDone = true;
                    return afterWaterMarkIterator.next();
                }
            } finally {
                lock.unlock();
            }
        }

        public void remove() {
            if (!waterMarkQueueDone) {
                waterMarkIterator.remove();
            } else {
                afterWaterMarkIterator.remove();
            }
        }
    }
}
