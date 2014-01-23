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

import java.io.ObjectStreamException;
import java.math.BigInteger;
import java.util.Random;

/**
 * Custom class for supporting primitive XSD data type nonNegativeInteger
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#nonNegativeInteger">XML Schema 3.3.20</a>
 */
public class NonNegativeInteger extends BigInteger {

    private static final long serialVersionUID = -484577860351406054L;

    public NonNegativeInteger(byte[] val) {
        super(val);
        checkValidity();
    } // ctor

    public NonNegativeInteger(int signum, byte[] magnitude) {
        super(signum, magnitude);
        checkValidity();
    } // ctor

    public NonNegativeInteger(int bitLength, int certainty, Random rnd) {
        super(bitLength, certainty, rnd);
        checkValidity();
    } // ctor

    public NonNegativeInteger(int numBits, Random rnd) {
        super(numBits, rnd);
        checkValidity();
    } // ctor

    public NonNegativeInteger(String val) {
        super(val);
        checkValidity();
    }

    public NonNegativeInteger(String val, int radix) {
        super(val, radix);
        checkValidity();
    } // ctor

    /** validate the value against the xsd definition */
    private BigInteger zero = new BigInteger("0");

    private void checkValidity() {
        if (compareTo(zero) < 0) {
            throw new NumberFormatException(
//                    Messages.getMessage("badNonNegInt00") +
":  " + this);
        }
    } // checkValidity

    /**
     * Work-around for http://developer.java.sun.com/developer/bugParade/bugs/4378370.html
     *
     * @return BigIntegerRep
     * @throws ObjectStreamException
     * @deprecated As per https://issues.apache.org/jira/browse/AXIS2-3848
     */
    public Object writeReplace() throws ObjectStreamException {
        return new BigIntegerRep(toByteArray());
    }

    /**
     * @deprecated As per https://issues.apache.org/jira/browse/AXIS2-3848
     */
    protected static class BigIntegerRep implements java.io.Serializable {
        private static final long serialVersionUID = -6135065605514678689L;
        private byte[] array;

        protected BigIntegerRep(byte[] array) {
            this.array = array;
        }

        protected Object readResolve() throws java.io.ObjectStreamException {
            return new NonNegativeInteger(array);
        }
    }
} // class NonNegativeInteger
