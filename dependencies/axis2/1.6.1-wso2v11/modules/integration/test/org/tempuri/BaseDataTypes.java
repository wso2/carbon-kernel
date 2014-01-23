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

package org.tempuri;

import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

public class BaseDataTypes {

    public boolean retBool(
            boolean inBool) {
        return inBool;
    }

    public short retByte(
            short inByte) {
        return inByte;
    }

    public byte retSByte(
            byte inSByte) {
        return inSByte;
    }

    public byte[] retByteArray(
            byte[] inByteArray) {
        return inByteArray;
    }

    public int retChar(
            int inChar) {
        return inChar;
    }

    public BigDecimal retDecimal(
            BigDecimal inDecimal) {
        return inDecimal;
    }

    public float retFloat(
            float inFloat) {
        return inFloat;
    }

    public double retDouble(
            double inDouble) {
        return inDouble;
    }

    public float retSingle(
            float inSingle) {
        return inSingle;
    }

    public int retInt(
            int inInt) {
        return inInt;
    }

    public short retShort(
            short inShort) {
        return inShort;
    }

    public long retLong(
            long inLong) {
        return inLong;
    }

    public Object retObject(
            Object inObject) {
        return inObject;
    }

    public long retUInt(
            long inUInt) {
        return inUInt;
    }

    public int retUShort(
            int inUShort) {
        return inUShort;
    }

    public BigInteger retULong(
            BigInteger inULong) {
        return inULong;
    }

    public String retString(
            String inString) {
        return inString;
    }

    public String retGuid(
            String inGuid) {
        return inGuid;
    }

    public String retUri(
            String inUri) {
        return inUri;
    }

    public Calendar retDateTime(
            Calendar inDateTime) {
        return inDateTime;
    }

    public Duration retTimeSpan(
            Duration inTimeSpan) {
        return inTimeSpan;
    }

    public QName retQName(
            QName inQName) {
        return inQName;
    }
}
