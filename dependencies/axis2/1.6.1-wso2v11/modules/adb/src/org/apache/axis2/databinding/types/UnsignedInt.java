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


/**
 * Custom class for supporting primitive XSD data type UnsignedInt
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#unsignedInt">XML Schema 3.3.22</a>
 */
public class UnsignedInt extends java.lang.Number {

    private static final long serialVersionUID = -8915204168154746305L;

    protected Long lValue = new Long(0);

    public UnsignedInt() {
    }

    /**
     * ctor for UnsignedInt
     *
     * @throws NumberFormatException will be thrown if validation fails
     */
    public UnsignedInt(long iValue) throws NumberFormatException {
        setValue(iValue);
    }

    public UnsignedInt(String stValue) throws NumberFormatException {
        setValue(Long.parseLong(stValue));
    }


    /**
     * validates the data and sets the value for the object.
     *
     * @param iValue value
     */
    public void setValue(long iValue) throws NumberFormatException {
        if (!UnsignedInt.isValid(iValue))
            throw new NumberFormatException(
//                    Messages.getMessage("badUnsignedInt00") +
String.valueOf(iValue) + "]");
        lValue = new Long(iValue);
    }

    public String toString() {
        if (lValue != null)
            return lValue.toString();
        else
            return null;
    }

    public int hashCode() {
        if (lValue != null)
            return lValue.hashCode();
        else
            return 0;
    }

    /** validate the value against the xsd definition */
    public static boolean isValid(long iValue) {
        return !((iValue < 0L) || (iValue > 4294967295L));
    }

    private Object __equalsCalc = null;

    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof UnsignedInt)) return false;
        UnsignedInt other = (UnsignedInt)obj;
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


}
