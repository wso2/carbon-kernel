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

package org.tempuri.complex;

import org.tempuri.complex.data.BitMask;
import org.tempuri.complex.data.Employee;
import org.tempuri.complex.data.Furniture;
import org.tempuri.complex.data.Group;
import org.tempuri.complex.data.Name;
import org.tempuri.complex.data.Person;
import org.tempuri.complex.data.Table;
import org.tempuri.complex.data.arrays.ArrayOfArrayOfstring;
import org.tempuri.complex.data.arrays.ArrayOfNullableOfdateTime;
import org.tempuri.complex.data.arrays.ArrayOfNullableOfdecimal;
import org.tempuri.complex.data.arrays.ArrayOfPerson;
import org.tempuri.complex.data.arrays.ArrayOfanyType;
import org.tempuri.complex.data.arrays.ArrayOfint;
import org.tempuri.complex.data.arrays.ArrayOfstring;

import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;


public class ComplexDataTypes {

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

    public int[] retInts(
            int[] inInt) {
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

    public String[] retStrings(
            String[] inString) {
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

    public QName retQName(
            QName inQName) {
        return inQName;
    }

    public ArrayOfstring retArrayString1D(
            ArrayOfstring inArrayString1D) {
        return inArrayString1D;
    }

    public ArrayOfint retArrayInt1D(
            ArrayOfint inArrayInt1D) {
        return inArrayInt1D;
    }

    public ArrayOfNullableOfdecimal retArrayDecimal1D(
            ArrayOfNullableOfdecimal inArrayDecimal1D) {
        return inArrayDecimal1D;
    }

    public ArrayOfNullableOfdateTime retArrayDateTime1D(
            ArrayOfNullableOfdateTime inArrayDateTime1D) {
        return inArrayDateTime1D;
    }

    public ArrayOfArrayOfstring retArrayString2D(
            ArrayOfArrayOfstring inArrayString2D) {
        return inArrayString2D;
    }

    public ArrayOfPerson retArray1DSN(
            ArrayOfPerson inArray1DSN) {
        return inArray1DSN;
    }

    public ArrayOfanyType retArrayAnyType1D(
            ArrayOfanyType inArrayAnyType1D) {
        return inArrayAnyType1D;
    }

    public Name retStructS1(
            Name inStructS1) {
        return inStructS1;
    }

    public Person retStructSN(
            Person inStructSN) {
        return inStructSN;
    }

    public Employee retStructSNSA(
            Employee inStructSNSA) {
        return inStructSNSA;
    }

    public Group retStructSNSAS(
            Group inStructSNSAS) {
        return inStructSNSAS;
    }

    public BitMask retEnumString(
            BitMask inEnumString) {
        return inEnumString;
    }

    public String retEnumInt(
            String inEnumInt) {
        return inEnumInt;
    }

    public Furniture retDerivedClass(
            Furniture inDerivedClass) {
        return inDerivedClass;
    }

    public Table retDerivedClass2(
            Table inDerivedClass) {
        return inDerivedClass;
    }
}
