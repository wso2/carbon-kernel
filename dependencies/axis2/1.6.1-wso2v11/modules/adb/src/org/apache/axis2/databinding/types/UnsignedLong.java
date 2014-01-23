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

package org.apache.axis2.databinding.types;

// Consider removing this.
// All operations behave as if BigIntegers were represented in two's-complement notation.
// In its place, consider using primitive type long (which is already the right size) to hold the data.
// This class can hide the fact that the data is stored in a signed entity, by careful implementation of the class' methods.

import java.math.BigInteger;

/**
 * Custom class for supporting primitive XSD data type UnsignedLong
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#unsignedLong">XML Schema 3.3.21</a>
 */
public class UnsignedLong extends java.lang.Number implements Comparable {

    private static final long serialVersionUID = -5919942584284897583L;

    protected BigInteger lValue = BigInteger.ZERO;
    private static BigInteger MAX = new BigInteger("18446744073709551615"); // max unsigned long

    public UnsignedLong() {
    }

    public UnsignedLong(double value) throws NumberFormatException {
        setValue(new BigInteger(Double.toString(value)));
    }

    public UnsignedLong(BigInteger value) throws NumberFormatException {
        setValue(value);
    }

    public UnsignedLong(long lValue) throws IllegalArgumentException {
        // new UnsignedLong( 0xffffffffffffffffL )
        // should not throw any Exception because, as an UnsignedLong, it is in range and nonnegative.
        setValue(BigInteger.valueOf(lValue));
    }

    public UnsignedLong(String stValue) throws NumberFormatException {

        // If stValue starts with a minus sign, that will be acceptable to the BigInteger constructor,
        // but it is not acceptable to us.
        // Once encoded into binary, it is too late to detect that the client intended a negative integer.
        // That detection must be performed here.
        try {
            if (stValue.charAt(0) == '\u002d') {
                throw new NumberFormatException(
                        "A String that starts with a minus sign is not a valid representation of an UnsignedLong.");
            }
            setValue(new BigInteger(stValue));
        }

        catch (NumberFormatException numberFormatException) {
            throw numberFormatException;
        }

        catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            // This could happen if stValue is empty when we attempt to detect a minus sign.
            // From the client's point of view, the empty String should cause a NumberFormatException.
            throw new NumberFormatException(
                    "An empty string is not a valid representation of an UnsignedLong.");
        }

    }

    private void setValue(BigInteger val) {
        if (!UnsignedLong.isValid(val)) {
            throw new IllegalArgumentException(
//                    Messages.getMessage("badUnsignedLong00") +
String.valueOf(val) + "]");
        }
        this.lValue = val;
    }

    public static boolean isValid(BigInteger value) {

        // Converts this BigInteger to a long.
        // This conversion is analogous to a narrowing primitive conversion from long to int as defined in the Java Language Specification:
        // if this BigInteger is too big to fit in a long, only the low-order 64 bits are returned.
        // Note that this conversion can lose information about the overall magnitude of the BigInteger value as well as return a result with the opposite sign.
        long unsignedLongValue = value.longValue();

        return !(compare(unsignedLongValue, BigInteger.ZERO.longValue()) < 0 || // less than zero
                compare(unsignedLongValue, MAX.longValue()) > 0);
    }

    public String toString() {
        return lValue.toString();
    }

    public int hashCode() {
        if (lValue != null)
            return lValue.hashCode();
        else
            return 0;
    }

    private Object __equalsCalc = null;

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof UnsignedLong)) return false;
        UnsignedLong other = (UnsignedLong)obj;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = ((lValue == null && other.lValue == null) ||
                (lValue != null &&
                        lValue.equals(other.lValue)));
        __equalsCalc = null;
        return _equals;
    }

    // Implement java.lang.Number interface
    public byte byteValue() {
        return lValue.byteValue();
    }

    public short shortValue() {
        return lValue.shortValue();
    }

    public int intValue() {
        return lValue.intValue();
    }

    public long longValue() {
        return lValue.longValue();
    }

    public double doubleValue() {
        return lValue.doubleValue();
    }

    public float floatValue() {
        return lValue.floatValue();
    }

    /**
     * @return the value 0 if the argument is an UnsignedLong numerically equal to this
     *         UnsignedLong; a value less than 0 if the argument is an UnsignedLong numerically
     *         greater than this UnsignedLong; and a value greater than 0 if the argument is an
     *         UnsignedLong numerically less than this UnsignedLong.
     */
    public int compareTo(Object o) {
        int retVal =
                0; // arbitrary default value in case of exception; required return value in case this object is equal to the specified object

        if (o == null || !(o instanceof UnsignedLong)) {
            throw new ClassCastException("The argument is not an UnsignedLong.");
        }
        // Only need to change retVal if this object is not equal to the specified object.
        retVal = compare(longValue(), ((UnsignedLong)o).longValue());

        return retVal;

    }

    /**
     * @return the value 0 if thatLong is a long numerically equal to thisLong; a value less than 0
     *         if thatLong is a long numerically greater than thisLong; and a value greater than 0
     *         if thatLong is a long numerically less than thisLong (unsigned comparison).
     */
    private static int compare(long thisLong, long thatLong) {
        // To avoid infinite recursion, do not instantiate UnsignedLong in this method, which may be called during UnsignedLong instantiation.

        if (thisLong == thatLong) {
            return 0;
        } else {
            boolean isLessThan; // This is less than that.

            // Prepare the most significant half of the data for comparison.
            // The shift distance can be any number from 1 to 32 inclusive (1 is probably fastest).
            // A shift distance of one is sufficient to move the significant data off of the sign bit, allowing for a signed comparison of positive numbers (i.e. an unsigned comparison).
            long thisHalfLong = (thisLong & 0xffffffff00000000L) >>> 1;
            long thatHalfLong = (thatLong & 0xffffffff00000000L) >>> 1;

            if (thisHalfLong == thatHalfLong) {
                // We must also look at the least significant half of the data.

                // Prepare the least significant half of the data for comparison.
                thisHalfLong = (thisLong & 0x00000000ffffffffL);
                thatHalfLong = (thatLong & 0x00000000ffffffffL);

                // We already know that the data is not equal.
                isLessThan = thisHalfLong < thatHalfLong;
            } else {
                // The answer is in the most significant half of the data.
                isLessThan = thisHalfLong < thatHalfLong;
            }

            if (isLessThan) {
                return -1; // Returns a negative integer as this object is less than than the specified object.
            } else {
                return 1; // Returns a positive integer as this object is greater than than the specified object.
            }
        }
    }

}
