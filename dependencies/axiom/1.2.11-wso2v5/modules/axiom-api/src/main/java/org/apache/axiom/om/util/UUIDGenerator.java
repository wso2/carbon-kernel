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

package org.apache.axiom.om.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

import org.apache.axiom.om.OMException;

/**
 * @deprecated Please use one of the specialized methods in the
 *             {@link org.apache.axiom.util.UIDGenerator} class. In contrast to what its name
 *             suggests, the {@link #getUUID()} method doesn't return a UUID. It also doesn't return
 *             a valid URN with uuid NID. See AXIS2-4527 for more information.
 */
public class UUIDGenerator {
    /** This class will give UUIDs for axis2. */

    private static String baseUUID = null;
    private static long incrementingValue = 0;


    private static Random myRand = null;

    /**
     * MD5 a random string with localhost/date etc will return 128 bits construct a string of 18
     * characters from those bits.
     *
     * @return string
     */
    public static synchronized String getUUID() {
        if (baseUUID == null) {
            baseUUID = getInitialUUID();
            baseUUID = "urn:uuid:" + baseUUID;
        }
        if (++incrementingValue >= Long.MAX_VALUE) {
            incrementingValue = 0;
        }
        return baseUUID + (System.currentTimeMillis() + incrementingValue);
    }

    protected static String getInitialUUID() {
        if (myRand == null) {
            myRand = new Random();
        }
        long rand = myRand.nextLong();
        String sid;
        try {
            sid = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            sid = Thread.currentThread().getName();
        }
        StringBuffer sb = new StringBuffer();
        sb.append(sid);
        sb.append(":");
        sb.append(Long.toString(rand));
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new OMException(e);
        }
        md5.update(sb.toString().getBytes());
        byte[] array = md5.digest();
        StringBuffer sb2 = new StringBuffer();
        for (int j = 0; j < array.length; ++j) {
            int b = array[j] & 0xFF;
            sb2.append(Integer.toHexString(b));
        }
        int begin = myRand.nextInt();
        if (begin < 0) begin = begin * -1;
        begin = begin % 8;
        return sb2.toString().substring(begin, begin + 18).toUpperCase();
    }

    public static void main(String[] args) {
        long startTime = new Date().getTime();
        for (int i = 0; i < 100000; i++) {
            UUIDGenerator.getInitialUUID();
        }
        long endTime = new Date().getTime();
        System.out.println("getInitialUUID Difference = " + (endTime - startTime));

        startTime = new Date().getTime();
        for (int i = 0; i < 100000; i++) {
            UUIDGenerator.getUUID();
        }
        endTime = new Date().getTime();
        System.out.println("getUUID Difference = " + (endTime - startTime));
    }
}
