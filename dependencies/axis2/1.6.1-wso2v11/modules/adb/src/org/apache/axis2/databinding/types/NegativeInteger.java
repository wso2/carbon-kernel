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
 * Custom class for supporting primitive XSD data type negativeinteger
 * <p/>
 * negativeInteger is derived from nonPositiveInteger by setting the value of maxInclusive to be -1.
 * This results in the standard mathematical concept of the negative integers. The value space of
 * negativeInteger is the infinite set {...,-2,-1}. The base type of negativeInteger is
 * nonPositiveInteger.
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#negativeInteger">XML Schema 3.3.15</a>
 */
public class NegativeInteger extends NonPositiveInteger {

    private static final long serialVersionUID = -2295928715693639427L;

    public NegativeInteger(byte[] val) {
        super(val);
        checkValidity();
    } // ctor

    public NegativeInteger(int signum, byte[] magnitude) {
        super(signum, magnitude);
        checkValidity();
    } // ctor

    public NegativeInteger(int bitLength, int certainty, Random rnd) {
        super(bitLength, certainty, rnd);
        checkValidity();
    } // ctor

    public NegativeInteger(int numBits, Random rnd) {
        super(numBits, rnd);
        checkValidity();
    } // ctor

    public NegativeInteger(String val) {
        super(val);
        checkValidity();
    }

    public NegativeInteger(String val, int radix) {
        super(val, radix);
        checkValidity();
    } // ctor

    /** validate the value against the xsd definition */
    private BigInteger zero = new BigInteger("0");

    private void checkValidity() {
        if (compareTo(zero) >= 0) {
            throw new NumberFormatException(
                    // Messages.getMessage("badnegInt00") +
                    ":  " + this);
        }
    } // checkValidity

    /**
     * Work-around for http://developer.java.sun.com/developer/bugParade/bugs/4378370.html
     *
     * @return BigIntegerRep
     * @throws java.io.ObjectStreamException
     * @deprecated As per https://issues.apache.org/jira/browse/AXIS2-3848
     */
    public Object writeReplace() throws ObjectStreamException {
        return new BigIntegerRep(toByteArray());
    }

    /**
     * @deprecated As per https://issues.apache.org/jira/browse/AXIS2-3848
     */
    protected static class BigIntegerRep implements java.io.Serializable {
        private static final long serialVersionUID = 1209618487031404110L;
        private byte[] array;

        protected BigIntegerRep(byte[] array) {
            this.array = array;
        }

        protected Object readResolve() throws java.io.ObjectStreamException {
            return new NegativeInteger(array);
        }
    }
} // class NonNegativeInteger
