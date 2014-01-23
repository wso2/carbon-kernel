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

package org.apache.axiom.attachments.utils;

/**
 * ByteSearch
 *
 * Various byte array searching utilities.
 * This includes a "skip search", which is a 
 * an optimized search for finding a byte pattern in 
 * a large byte array.
 *
 * @author Richard Scheuerle (scheu@us.ibm.com)
 *
 */
public class ByteSearch {

    
    /**
     * Search a byte sequence for a given pattern. The method uses the
     * skip search algorithm. The search can be performed in forward
     * or backward direction, i.e. beginning from the start or end of the
     * byte sequence.
     * 
     * @param pattern  byte[] 
     * @param direction true if forward, false if backward
     * @param buffer byte[] to search
     * @param start index to start search
     * @param end index to end search (end index is not within the search)
     * @param skip short[256]  A skipArray generated from a call to 
     * generateSkipArray. 
     * @return index or -1 if not found
     */
    public static int skipSearch(byte[] pattern,
                                 boolean direction,
                                 byte[] buffer,
                                 int start,
                                 int end,
                                 short[] skip) {
        
        int patternLength = pattern.length;
        
        // If patternLength is larger than buffer,
        // return not found
        if (patternLength > (end - start)) {
            return -1;
        }
        
        if (direction) {
            int k = 0;
            for (k = start + patternLength - 1; 
                k < end; // end is exclusive
                k += skip[buffer[k] & (0xff)])   // SKIP NOTE below
            {
                try {
                    // k is the location in the buffer
                    // that may match the last byte in the pattern.
                    if (isEqual(pattern, buffer, (k-patternLength)+1, end)) {
                        return (k-patternLength)+1;
                    }
                    // SKIP NOTE: The next k index is calculated from 
                    // the skip array.  Basically if the k byte is not
                    // within the pattern, we skip ahead the length of the
                    // pattern.  Otherwise we skip ahead a distance less
                    // than the length.
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw e;
                }
            }
        } else {
            for (int k = end - patternLength; 
                k <= start;
                k -= skip[buffer[k] & (0xff)]) 
            {
                try {
                    // k is the location in the buffer
                    // that may match the first byte in the pattern.
                    if (isEqual(pattern, buffer, k, end)) {
                        return k;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw e;
                }
            }
        }
        return -1;            
    }
    
    /**
     * skipArray
     * Builds a skip array for this pattern and direction.
     * The skipArray is used in the optimized skipSearch
     * @param pattern
     * @param direction
     * @return short[256]
     */
    public static short[] getSkipArray(byte[] pattern,
                    boolean direction) {
        
        // The index key is a byte.
        // The short[key] is the number of bytes that can
        // be skipped that won't match the pattern
        short[] skip = new short[256];

        // If a byte is not within pattern, then we can
        // skip ahead the entire length of the pattern.
        // So fill the skip array with the pattern length
        java.util.Arrays.fill(skip, (short) pattern.length);

        if (direction) {
            // If the byte is found in the pattern,
            // this affects how far we can skip.
            // The skip distance is the distance of the
            // character from the end of the pattern.
            // The last character in the pattern is excluded.
            for (int k = 0; k < pattern.length -1; k++) {
                skip[pattern[k] & (0xff)] = (short)(pattern.length - k - 1);
            }
        } else {
            for (int k = pattern.length-2; k >= 0; k--) {
                skip[pattern[k] &(0xff)] = (short)(pattern.length - k - 1);
            }
        }
        return skip;
    }
    
    /**
     * 
     * isEqual
     * @param pattern
     * @param buffer
     * @param start index
     * @param end index
     * @return true if the bytes in buffer[start] equal pattern
     * 
     */
    public static boolean isEqual(byte[] pattern, byte[] buffer, int start, int end) {
//        if (pattern.length >= end-start) {
        if (pattern.length > end-start) {
            return false;
        }
        for (int j=0; j<pattern.length; j++) {
            if (pattern[j] != buffer[start+j]) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * search
     * Look for the search bytes in the bytes array using a straight search.
     * Use the skip search if the number of bytes to search is large or
     * if this search is performed frequently.
     * @param search byte[]
     * @param bytes byte[] to search
     * @param start starting index
     * @param end end index (exclusive
     * @param direction boolean (true indicates forward search, false is backwards search
     * @return index or -1
     */
    public static int search(byte[] search, 
            byte[] bytes, int start, int end, 
            boolean direction) {
        
        int idx = -1;
        if (search == null || search.length == 0 ||
            bytes == null || bytes.length == 0 ||
            start < 0 || end <= 0) {
            return idx;
        }
        
        // Search byte bytes array
        if (direction) {
            for (int i=start; idx < 0 && i< end; i++) {
                if (bytes[i] == search[0]) {
                    // Potential match..check remaining bytes
                    boolean found = true;  // assume found
                    for (int i2=1; found && i2<search.length; i2++) {
                        if (i+i2 >= end) {
                            found = false;
                        } else {
                            found = (bytes[i+i2] == search[i2]); 
                        }
                    }
                    // If found match, set return idx
                    if (found) {
                        idx = i;
                    }
                }
            }
        } else {
            for (int i=end-1; idx < 0 && i>=start; i--) {
                if (bytes[i] == search[0]) {
                    // Potential match..check remaining bytes
                    boolean found = true;  // assume found
                    for (int i2=1; found && i2<search.length; i2++) {
                        if (i+i2 >= end) {
                            found = false;
                        } else {
                            found = (bytes[i+i2] == search[i2]); 
                        }
                    }
                    // If found match, set return idx
                    if (found) {
                        idx = i;
                    }
                }
            }
        }

        return idx;
    }
}

 
