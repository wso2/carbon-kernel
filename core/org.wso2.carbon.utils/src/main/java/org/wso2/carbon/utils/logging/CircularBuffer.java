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
public class CircularBuffer<K> {
    private List<K> bufferList;
    private static final int MAX_ALLOWED_SIZE = 10000;
    private int startIndex;
    private int endIndex;
    private final int capacity;

    public CircularBuffer(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Requested size of circular buffer (" + size + ") is invalid");
        }
        if (size > MAX_ALLOWED_SIZE) {
            throw new IllegalArgumentException("Requested size of circular buffer (" + size + ") is greater than the " +
                    "allowed max size " + MAX_ALLOWED_SIZE);
        }
        capacity = size;
        bufferList = new ArrayList<K>(capacity);
        startIndex = 0;
        endIndex = -1;
    }

    public CircularBuffer() {
        this(MAX_ALLOWED_SIZE);
    }

    public synchronized void append(K element) {
        if (element == null) {
            throw new NullPointerException("Circular buffer doesn't support null values to be added to buffer");
        }
        if (startIndex == capacity - 1) {
            startIndex = 0;
        } else if (endIndex == capacity - 1) {
            endIndex = -1;
            startIndex = 1;
        } else if (startIndex != 0) {   // start index is not in beginning of the buffer
            startIndex++;
        }
        endIndex++;
        bufferList.add(endIndex, element);
    }

    public synchronized List<K> get(int amount) {
        List<K> result;
        if (startIndex == 0) { // simple case. startIndex is beginning of the buffer
            if (endIndex + 1 >= amount) {
                result = new ArrayList<K>(amount);
                for (int i = startIndex; i < amount; i++) {
                    result.add(bufferList.get(i));
                }
            } else { // amount to be retrieved is more than the total size of buffer array
                result = new ArrayList<K>(endIndex + 1);
                for (int i = startIndex; i < endIndex; i++) {
                    result.add(bufferList.get(i));
                }
            }
        } else { // starIndex is in the middle or end of the buffer array.
            // Note that buffer array is completely fille in this case.
            if (amount < bufferList.size()) {// amount to be retrieved is less than the total size of contents of buffer
                result = new ArrayList<K>(amount);
                if (amount <= bufferList.size() - startIndex) { // no. of items remaining to the
                    //  right of startIndex is less than the amount to be retrieved
                    for (int i = startIndex; i < amount; i++) {
                        result.add(bufferList.get(i));
                    }
                } else {
                    for (int i = startIndex; i < bufferList.size(); i++) {
                        result.add(bufferList.get(i));
                    }
                    for (int i = 0; i < (amount - (bufferList.size() - startIndex)); i++) {
                        result.add(bufferList.get(i));
                    }
                }
            } else {// amount to be retrieved is more than the total size of buffer
                result = new ArrayList<K>(bufferList.size());
                for (int i = startIndex; i < bufferList.size(); i++) {
                    result.add(bufferList.get(i));
                }
                for (int i = 0; i < endIndex; i++) {
                    result.add(bufferList.get(i));
                }
            }
        }
        return result;
    }

    public synchronized Object[] getObjects(int amount){
        List<K> objectsList = get(amount);
        return objectsList.toArray(new Object[objectsList.size()]);
    }

    public synchronized void clear() {
        bufferList.clear();
        startIndex = 0;
        endIndex = -1;
    }
}