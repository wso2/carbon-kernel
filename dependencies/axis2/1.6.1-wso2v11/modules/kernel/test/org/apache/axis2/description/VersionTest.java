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

package org.apache.axis2.description;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class VersionTest extends TestCase {
    private static final String[] testVersionSequence = {
        "0.1",
        "0.2",
        "1.0",
        "1.1-beta1",
        "1.1-beta2",
        "1.1-RC1",
        "1.1-RC2",
        "1.1-SNAPSHOT",
        "1.1",
        "1.1.1",
        "1.1.2",
        "1.2",
        "SNAPSHOT"
    };
    
    private static int sign(int v) {
        if (v < 0) {
            return -1;
        } else if (v > 0) {
            return 1;
        } else {
            return 0;
        }
    }
    
    public void testCompareTo() throws Exception {
        for (int i1=0; i1<testVersionSequence.length; i1++) {
            for (int i2=0; i2<testVersionSequence.length; i2++) {
                Version v1 = new Version(testVersionSequence[i1]);
                Version v2 = new Version(testVersionSequence[i2]);
                assertEquals(v1 + " vs. " + v2, sign(i1-i2), sign(v1.compareTo(v2)));
            }
        }
    }
    
    public void testToString() throws Exception {
        for (int i=0; i<testVersionSequence.length; i++) {
            assertEquals(testVersionSequence[i], new Version(testVersionSequence[i]).toString());
        }
    }
    
    public void testEquals() throws Exception {
        for (int i1=0; i1<testVersionSequence.length; i1++) {
            for (int i2=0; i2<testVersionSequence.length; i2++) {
                Version v1 = new Version(testVersionSequence[i1]);
                Version v2 = new Version(testVersionSequence[i2]);
                assertEquals(v1 + " vs. " + v2, i1 == i2, v1.equals(v2));
            }
        }
    }
    
    public void testHashCode() throws Exception {
        Set<Integer> values = new HashSet<Integer>();
        for (int i=0; i<testVersionSequence.length; i++) {
            values.add(new Version(testVersionSequence[i]).hashCode());
        }
        // Check that the collision frequency is less than 10%
        assertTrue(values.size() > testVersionSequence.length*9/10);
    }
}
