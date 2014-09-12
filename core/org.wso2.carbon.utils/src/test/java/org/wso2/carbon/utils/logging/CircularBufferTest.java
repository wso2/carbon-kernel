/*
 * Copyright 2005,2014 WSO2, Inc. http://www.wso2.org
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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 *
 */
public class CircularBufferTest {
    private static final int MAX_ALLOWED_SIZE = 10000;

    /**
     * Test if the append method has appended an element to the buffer by returning that element
     * from the buffer
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testAppend() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(5);
        assertEquals(buffer.getSizeOfBuffer(), 5, "Buffer is not initialized with expected size.");
        buffer.append("item");
        assertNotNull(buffer.get(1), "Items were not appended.");
    }

    /***
     * Test if appending a null element throws a NullPointerException
     */
    @Test(expectedExceptions = NullPointerException.class)
    public void testAppendNullElement() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(5);
        assertEquals(buffer.getSizeOfBuffer(), 5, "Buffer is not initialized with expected size.");
        buffer.append(null);
    }

    /**
     * Test whether the buffer can return amount of elements less than the capacity of the buffer
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testGetLessThanBufferCapacity1() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(10);
        assertEquals(buffer.getSizeOfBuffer(), 10, "Buffer is not initialized with expected size.");
        for (int i = 0; i <= 11; i++) {
            buffer.append("item" + i);
        }
        assertEquals(buffer.get(5).size(), 5, "Returned an unexpected amount!");
    }

    /**
     * Test whether when 0 elements are retrieved, 0 elements are returned
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testGetLessThanBufferCapacity2() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(10);
        assertEquals(buffer.getSizeOfBuffer(), 10, "Buffer is not initialized with expected size.");
        for (int i = 0; i <= 11; i++) {
            buffer.append("item" + i);
        }
        assertEquals(buffer.get(0).size(), 0,
                     "Returned an unexpected amount! Should return 0 elements.");
    }

    /**
     * Test whether when 1 element is retrieved, 1 element is returned
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testGetLessThanBufferCapacity3() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(10);
        assertEquals(buffer.getSizeOfBuffer(), 10, "Buffer is not initialized with expected size.");
        for (int i = 0; i <= 11; i++) {
            buffer.append("item" + i);
        }
        assertEquals(buffer.get(1).size(), 1, "Returned an unexpected amount!");
    }

    /**
     * Test returning no. of items remaining to the right of startIndex is greater than the amount
     * to be retrieved
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testGetLessThanBufferCapacity4() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(10);
        assertEquals(buffer.getSizeOfBuffer(), 10, "Buffer is not initialized with expected size.");
        for (int i = 0; i <= 11; i++) {
            buffer.append("item" + i);
        }
        assertEquals(buffer.get(8).size(), 8, "Returned an unexpected amount!");
    }

    /**
     * Test returning no. of items remaining to the right of startIndex is less than the amount to
     * be retrieved
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testGetLessThanBufferCapacity5() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(10);
        assertEquals(buffer.getSizeOfBuffer(), 10, "Buffer is not initialized with expected size.");
        for (int i = 0; i <= 15; i++) {
            buffer.append("item" + i);
        }
        assertEquals(buffer.get(8).size(), 8, "Returned an unexpected amount!");
    }

    /**
     * Test whether the buffer can return amount of elements less than the capacity of the buffer if
     * it is not completely filled
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testGetLessThanBufferCapacity6() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(10);
        assertEquals(buffer.getSizeOfBuffer(), 10, "Buffer is not initialized with expected size.");
        for (int i = 0; i <= 5; i++) {
            buffer.append("item" + i);
        }
        assertEquals(buffer.get(5).size(), 5, "Returned an unexpected amount!");
    }

    /**
     * Test whether the buffer can return amount of elements less than the capacity of the buffer if
     * it is not completely filled
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testGetLessThanBufferCapacity7() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(10);
        assertEquals(buffer.getSizeOfBuffer(), 10, "Buffer is not initialized with expected size.");
        for (int i = 0; i <= 5; i++) {
            buffer.append("item" + i);
        }
        assertEquals(buffer.get(3).size(), 3, "Returned an unexpected amount!");
    }

    /**
     * Test whether the buffer can return same amount as the capacity of the buffer if it is fully
     * filled without exercising the circular nature
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testGetEqualToBufferCapacity1() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(10);
        assertEquals(buffer.getSizeOfBuffer(), 10, "Buffer is not initialized with expected size.");
        for (int i = 0; i <= 11; i++) {
            buffer.append("item" + i);
        }
        assertEquals(buffer.get(10).size(), 10, "Returned an unexpected amount!");
    }

    /**
     * Test whether the buffer can return same amount as the capacity of the buffer if it is fully
     * filled
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testGetEqualToBufferCapacity2() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(10);
        assertEquals(buffer.getSizeOfBuffer(), 10, "Buffer is not initialized with expected size.");
        for (int i = 0; i <= 15; i++) {
            buffer.append("item" + i);
        }
        assertEquals(buffer.get(10).size(), 10, "Returned an unexpected amount!");
    }

    /**
     * Test how many elements the buffer return, if an amount more than its capacity is retrieved
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testGetMoreThanBufferCapacity1() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(5);
        assertEquals(buffer.getSizeOfBuffer(), 5, "Buffer is not initialized with expected size.");
        for (int i = 0; i <= 11; i++) {
            buffer.append("item" + i);
        }
        assertEquals(buffer.get(8).size(), 5,
                     "Returned an unexpected amount! Should have returned the buffer capacity " +
                     "instead.");
    }

    /**
     * This test is created to identify an edge case. When the buffer is initialized to any amount
     * (eg: 5) and then any amount higher than that (eg: 12) is added to the buffer, and later when
     * an amount (1 less than the inserted number of items, eg: 10 ) is retrieved the buffer should
     * not return a wrong number of items.
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testGetMoreThanBufferCapacity2() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(5);
        assertEquals(buffer.getSizeOfBuffer(), 5, "Buffer is not initialized with expected size.");
        for (int i = 0; i <= 11; i++) {
            buffer.append("item" + i);
        }
        assertEquals(buffer.get(10).size(), 5,
                     "Returned an unexpected amount! Should have returned the buffer capacity " +
                     "instead.");
    }

    /**
     * Test how many elements the buffer return, if an amount more than its capacity is retrieved
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testGetMoreThanBufferCapacity3() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(10);
        assertEquals(buffer.getSizeOfBuffer(), 10, "Buffer is not initialized with expected size.");
        for (int i = 0; i < 10; i++) {
            buffer.append("item" + i);
        }
        assertEquals(buffer.get(15).size(), 10,
                     "Returned an unexpected amount! Should have returned the buffer capacity " +
                     "instead.");
    }

    /**
     * Test if the buffer returns an empty list if a negative amount is requested.
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testGetNegativeAmount() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(5);
        assertEquals(buffer.getSizeOfBuffer(), 5, "Buffer is not initialized with expected size.");
        for (int i = 0; i <= 10; i++) {
            buffer.append("item" + i);
        }
        assertEquals(buffer.get(-1).size(), 0,
                     "Returned an unexpected amount! Should have returned the buffer capacity " +
                     "instead.");
    }

    /**
     * Test throwing an IllegalArgumentException when 0 sized buffer is initialized.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIllegalArgumentExceptionFromSizeZero() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(0);
    }

    /**
     * Test initializing a maximum sized buffer.
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testInitializingMaximumSizedBuffer() {
        CircularBuffer<String> buffer = new CircularBuffer<String>();
        assertEquals(buffer.getSizeOfBuffer(), MAX_ALLOWED_SIZE,
                     "Buffer is not initialized with maximum allowed size.");
    }

    /**
     * Test throwing an IllegalArgumentException when the buffer is initialized with more than
     * allowed maximum size.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIllegalArgumentExceptionSizeGreaterThanMaxAllowed() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(MAX_ALLOWED_SIZE + 1000);
    }

    /**
     * Test returning an object array with the given amount.
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testGetObjects() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(5);
        assertEquals(buffer.getSizeOfBuffer(), 5, "Buffer is not initialized with expected size.");
        for (int i = 0; i <= 10; i++) {
            buffer.append("item" + i);
        }
        Object[] expected = new Object[3];
        expected[0] = "item6";
        expected[1] = "item7";
        expected[2] = "item8";

        Object[] result = buffer.getObjects(3);

        assertEquals(result.length, 3, "Unexpected number of items was returned.");
        assertEquals(result, expected, "Unexpected objects array was returned.");
    }

    /**
     * Test clearing the buffer after populating it. This should result in an empty buffer.
     */
    @Test(groups = {"org.wso2.carbon.utils.logging"},
          description = "")
    public void testClear() {
        CircularBuffer<String> buffer = new CircularBuffer<String>(5);
        assertEquals(buffer.getSizeOfBuffer(), 5, "Buffer is not initialized with expected size.");
        for (int i = 0; i <= 10; i++) {
            buffer.append("item" + i);
        }
        assertEquals(buffer.get(3).size(), 3, "Returned an unexpected amount!");
        buffer.clear();
        assertEquals(buffer.get(3).size(), 0, "Returned an unexpected amount!");
    }
}
