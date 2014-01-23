/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axiom.util;

import java.util.Random;

/**
 * Unique ID generator implementation. This class generates unique IDs based on
 * the assumption that the following triplet is unique:
 * <ol>
 * <li>The thread ID.
 * <li>The timestamp in milliseconds when the first UID is requested by the
 * thread.
 * <li>A per thread sequence number that is incremented each time a UID is
 * requested by the thread.
 * </ol>
 * <p>
 * Considering that these three numbers are represented as <code>long</code>
 * values, these assumptions are correct because:
 * <ul>
 * <li>The probability that two different threads with the same ID exist in the
 * same millisecond interval is negligibly small.
 * <li>One can expect that no thread will ever request more than 2^64 UIDs
 * during its lifetime.
 * </ul>
 * <p>
 * Before building an ID from this triplet, the implementation will XOR the
 * three values with random values calculated once when the class is loaded.
 * This transformation preserves the uniqueness of the calculated triplet and
 * serves several purposes:
 * <ul>
 * <li>It reduces the probability that the same ID is produces by two different
 * systems, i.e. it increases global uniqueness.
 * <li>It adds entropy, i.e. it makes an individual ID appear as random. Indeed,
 * without the XOR transformation, a hexadecimal representation of the triplet
 * would in general contain several sequences of '0'.
 * <li>It prevents the implementation from leaking information about the system
 * state.
 * </ul>
 */
class UIDGeneratorImpl {
    private static final long startTimeXorOperand;
    private static final long threadIdXorOperand;
    private static final long seqXorOperand;
    
    static {
        Random rand = new Random();
        threadIdXorOperand = rand.nextLong();
        startTimeXorOperand = rand.nextLong();
        seqXorOperand = rand.nextLong();
    }
    
    private final String suffix;
    private long seq;
    
    UIDGeneratorImpl() {
        long xoredThreadId = Thread.currentThread().getId() ^ threadIdXorOperand;
        long xoredStartTime = System.currentTimeMillis() ^ startTimeXorOperand;
        StringBuilder buffer = new StringBuilder();
        writeReverseLongHex(xoredStartTime, buffer);
        writeReverseLongHex(xoredThreadId, buffer);
        suffix = buffer.toString();
    }
    
    private void writeReverseLongHex(long value, StringBuilder buffer) {
        for (int i=0; i<16; i++) {
            int n = (int)(value >> (4*i)) & 0xF;
            buffer.append((char)(n < 10 ? '0' + n : 'a' + n - 10));
        }
    }
    
    /**
     * Generate a unique ID as hex value and add it to the given buffer. Note
     * that with respect to the triplet, the order of nibbles is reversed, i.e.
     * the least significant nibble of the sequence is written first. This makes
     * comparing two IDs for equality more efficient.
     * 
     * @param buffer
     */
    void generateHex(StringBuilder buffer) {
        writeReverseLongHex(seq++ ^ seqXorOperand, buffer);
        buffer.append(suffix);
    }
}