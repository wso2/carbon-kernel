/* 
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.wso2.carbon.utils.logging;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a Circular Buffer implementation. In this implementaion it is assumed that items
 * will never be removed from this buffer. This can be used for a case such as a Rolling Log.
 * A client can request the latest 'n' number of items that are stored in this buffer.
 */
public class CircularBuffer<E> {
    private List<E> bufferList;
    private static final int MAX_ALLOWED_SIZE = 10000;
    private int startIndex;
    private int endIndex;
    private final int size;

    /**
     * Create a circular buffer with the given size
     *
     * @param size
     *         - fixed size of the buffer
     */
    public CircularBuffer(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Requested size of circular buffer (" + size + ") is invalid");
        }
        if (size > MAX_ALLOWED_SIZE) {
            throw new IllegalArgumentException("Requested size of circular buffer (" + size + ") is greater than the " +
                    "allowed max size " + MAX_ALLOWED_SIZE);
        }
        this.size = size;
        this.bufferList = new ArrayList<E>(getSize());
        this.startIndex = 0;
        this.endIndex = -1;
    }

    /**
     * Create a circular buffer with the maximum allowed size
     */
    public CircularBuffer() {
        this(MAX_ALLOWED_SIZE);
    }

    /**
     * Append elements while preserving the circular nature of the buffer.
     *
     * @param element
     *         - element to be appended
     */
    public synchronized void append(E element) {
        if (element == null) {
            throw new NullPointerException("Circular buffer doesn't support null values to be added to buffer");
        }
        if (startIndex == getSize() - 1) {
            startIndex = 0;
        } else if (endIndex == getSize() - 1) {
            endIndex = -1;
            startIndex = 1;
        } else if (startIndex != 0) {
            // start index is not in beginning of the buffer
            startIndex++;
        }
        endIndex++;
        if (getSize() == bufferList.size()) {
            // if the buffer capacity has been reached, replace the existing elements,
            // set method replaces the element in the given index
            bufferList.set(endIndex, element);
        } else {
            // if the buffer capacity has not been reached add elements to the list,
            // add method lets the array list grow, and appends elements to the end of list
            bufferList.add(endIndex, element);
        }
    }

    /**
     * Retrieve the given amount of elements from the circular buffer. This is a forgiving
     * operation, if the amount asked is greater than the size of the buffer it will return all the
     * available elements
     *
     * @param amount
     *         - no of elements to return
     * @return - a list of elements
     */
    public synchronized List<E> get(int amount) {
        if (amount < 0) {
            // if a negative amount is requested send an empty list
            return new ArrayList<E>();
        }
        List<E> result;
        if (startIndex == 0) {
            // simple case. startIndex is beginning of the buffer
            if (endIndex + 1 >= amount) {
                result = new ArrayList<E>(amount);
                for (int i = startIndex; i < amount; i++) {
                    result.add(bufferList.get(i));
                }
            } else {
                // amount to be retrieved is more than the capacity of the buffer
                result = new ArrayList<E>(endIndex + 1);
                for (int i = startIndex; i <= endIndex; i++) {
                    result.add(bufferList.get(i));
                }
            }
        } else {
            // starIndex is in the middle or end of the buffer.
            // Note that buffer is completely filled in this case.
            if (amount <= getSize()) {
                result = getListIfAmountLessThanOrEqualSizeOfBuffer(amount);
            } else {
                result = getListIfAmountGreaterThanSizeOfBuffer();
            }
        }
        return result;
    }

    /**
     * Get the list if the amount to be retrieved is more than the capacity of the buffer
     *
     * @return - list of elements
     */
    private List<E> getListIfAmountGreaterThanSizeOfBuffer() {
        List<E> result;
        result = new ArrayList<E>(getSize());
        // make sure the order is preserved by starting to copy from the start index
        // until capacity and then copy from beginning to end index
        for (int i = startIndex; i < getSize(); i++) {
            result.add(bufferList.get(i));
        }
        for (int i = 0; i <= endIndex; i++) {
            result.add(bufferList.get(i));
        }
        return result;
    }

    /**
     * Get the list if the amount to be retrieved is less than the capacity of the buffer
     *
     * @param amount
     *         - amount of elements to return from the buffer
     * @return - list of elements
     */
    private List<E> getListIfAmountLessThanOrEqualSizeOfBuffer(int amount) {
        List<E> result;
        result = new ArrayList<E>(amount);
        if (amount <= getSize() - startIndex) {
            // no. of items remaining to the
            // right of startIndex is greater than the amount to be retrieved
            int tempEndIndexToRead = amount + startIndex;
            // copy "amount" number of elements starting from the start index
            for (int i = startIndex; i < tempEndIndexToRead; i++) {
                result.add(bufferList.get(i));
            }
        } else {
            // no of elements remaining to the right of start index is less than the
            // amount to be retrieved
            for (int i = startIndex; i < getSize(); i++) {
                result.add(bufferList.get(i));
            }
            int tempAmountFromEndIndexToStartIndex = amount - (getSize() - startIndex);
            for (int i = 0; i < tempAmountFromEndIndexToStartIndex; i++) {
                result.add(bufferList.get(i));
            }
        }
        return result;
    }

    /**
     * This method is added for backward compatibility.
     *
     * @param amount
     *         - amount of elements to return from the buffer
     * @return - an object array of amount number of elements in the buffer
     */
    public synchronized Object[] getObjects(int amount) {
        List<E> objectsList = get(amount);
        return objectsList.toArray(new Object[objectsList.size()]);
    }

    /**
     * Clear the circular buffer and reset the indices.
     */
    public synchronized void clear() {
        bufferList.clear();
        startIndex = 0;
        endIndex = -1;
    }

    /**
     * Return the capacity of the circular buffer. This is set during buffer initialization.
     *
     * @return - capacity of the buffer
     */
    public int getSize() {
        return size;
    }
}