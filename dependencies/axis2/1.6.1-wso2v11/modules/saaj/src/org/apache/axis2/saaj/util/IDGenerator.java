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

package org.apache.axis2.saaj.util;

import java.util.Random;

/** Code borrowed from AuthenticatorBase.java for generating a secure id's. */
public class IDGenerator {

    /** The number of random bytes to include when generating a session identifier. */
    protected static final int SESSION_ID_BYTES = 16;

    /** A random number generator to use when generating session identifiers. */
    protected static Random random = null;

    /**
     * The Java class name of the random number generator class to be used when generating session
     * identifiers.
     */
    protected static String randomClass = "java.security.SecureRandom";

    /**
     * Generate and return a new session identifier.
     *
     * @return a new session id
     */
    public static synchronized String generateID() {
        // Generate a byte array containing a session identifier
        byte bytes[] = new byte[SESSION_ID_BYTES];

        getRandom().nextBytes(bytes);

        // Render the result as a String of hexadecimal digits
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < bytes.length; i++) {
            byte b1 = (byte)((bytes[i] & 0xf0) >> 4);
            byte b2 = (byte)(bytes[i] & 0x0f);

            if (b1 < 10) {
                result.append((char)('0' + b1));
            } else {
                result.append((char)('A' + (b1 - 10)));
            }
            if (b2 < 10) {
                result.append((char)('0' + b2));
            } else {
                result.append((char)('A' + (b2 - 10)));
            }
        }
        return (result.toString());
    }

    /**
     * Return the random number generator instance we should use for generating session identifiers.
     * If there is no such generator currently defined, construct and seed a new one.
     *
     * @return Random object
     */
    private static synchronized Random getRandom() {
        if (random == null) {
            try {
                Class clazz = Class.forName(randomClass);
                random = (Random)clazz.newInstance();
            } catch (Exception e) {
                random = new java.util.Random();
            }
        }
        return (random);
    }

}

