/**
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

package org.apache.ws.security.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * UUID generator (taken from Axis2)
 */
public class UUIDGenerator {

    private static final Log log = LogFactory.getLog(UUIDGenerator.class);

    private static String baseUUID = null;
    private static long incrementingValue = 0;


    private static Random myRand = null;

    /**
     * MD5 a random string with localhost/date etc will return 128 bits
     * construct a string of 18 characters from those bits.
     *
     * @return string
     */
    public static synchronized String getUUID() {
        if (baseUUID == null) {
            getInitialUUID();
        }
        long i = ++incrementingValue;
        if(i >= Long.MAX_VALUE || i < 0){
            incrementingValue = 0;
            i = 0;
        }
        return baseUUID + System.currentTimeMillis() + i;
    }

    protected static synchronized void getInitialUUID() {
        if (baseUUID != null) {
            return;
        }
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
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            //todo have to be properly handled
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
        baseUUID = sb2.toString().substring(begin, begin + 18).toUpperCase();
    }

}
