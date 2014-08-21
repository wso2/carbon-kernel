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

/**
 * This is a Circular Buffer implementation. In this implementaion it is assumed that items
 * will never be removed from this buffer. It can be used for a case such as a Rolling Log.
 * A client can request the latest 'n' number of items that are stored in this buffer.
 */
public class CircularBuffer {
    private Object[] buffer;
    private static final int MAX_ALLOWED_SIZE = 10000;
    private int startIndex;
    private int endIndex;

    public CircularBuffer(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Requested size of circular buffer (" +
                    size + ") is invalid");
        }
        if (size > MAX_ALLOWED_SIZE) {
            throw new IllegalArgumentException("Requested size of circular buffer (" +
                    size + ") is greater than the allowed max size " +
                    MAX_ALLOWED_SIZE);
        }
        buffer = new Object[size];
        startIndex = 0;
        endIndex = -1;
    }

    public CircularBuffer() {
        this(MAX_ALLOWED_SIZE);
    }

    public synchronized void append(Object obj) {
        if (startIndex == buffer.length - 1) { // are we at the end of the buffer?
            startIndex = 0;
        } else if (endIndex == buffer.length - 1) {
            endIndex = -1;
            startIndex = 1;
        } else if (startIndex != 0) {
            startIndex++;
        }
        endIndex++;
        buffer[endIndex] = obj;
    }

    public synchronized Object[] getObjects(int amount) {
        Object[] result;
        if (startIndex == 0) { // simple case. startIndex is beginning of buffer array              
            if (endIndex + 1 >= amount) {// amount to be retrieved is less than the
                // total size of contents of buffer array
                result = new Object[amount];
                System.arraycopy(buffer, 0, result, 0, amount);
            } else { // amount to be retrieved is more than the total size of buffer array
                result = new Object[endIndex + 1];
                System.arraycopy(buffer, 0, result, 0, endIndex + 1);
            }

        } else { // starIndex is in the middle or end of the buffer array.
            // Note that buffer array is completely fille in this case.
            if (amount < buffer.length) {// amount to be retrieved is less than the total size of
                // contents of buffer array
                result = new Object[amount];
                if (amount <= buffer.length - startIndex) { // no. of items remaining to the
                    //  right of startIndex is less than the amount to be retrieved
                    System.arraycopy(buffer, startIndex, result, 0, amount);
                } else {
                    System.arraycopy(buffer, startIndex, result, 0, buffer.length - startIndex);
                    System.arraycopy(buffer, 0, result, buffer.length - startIndex,
                            amount - buffer.length + startIndex);
                }
            } else {// amount to be retrieved is more than the total size of buffer array
                result = new Object[buffer.length];
                System.arraycopy(buffer, startIndex, result, 0, buffer.length - startIndex);
                System.arraycopy(buffer, 0, result, buffer.length - startIndex, endIndex + 1);
            }
        }
        return result;
    }

    public synchronized void clear() {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = null;
        }
        startIndex = 0;
        endIndex = -1;
    }
}