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
 * Custom class for supporting primitive XSD data type UnsignedByte
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#unsignedByte">XML Schema 3.3.24</a>
 */
public class UnsignedByte extends UnsignedShort {


    private static final long serialVersionUID = 4631754787145526759L;

    public UnsignedByte() {

    }

    /**
     * ctor for UnsignedByte
     *
     * @throws Exception will be thrown if validation fails
     */
    public UnsignedByte(long sValue) throws NumberFormatException {
        setValue(sValue);
    }

    public UnsignedByte(String sValue) throws NumberFormatException {
        setValue(Long.parseLong(sValue));
    }

    /**
     * validates the data and sets the value for the object.
     *
     * @param sValue the number to set
     */
    public void setValue(long sValue) throws NumberFormatException {
        if (!UnsignedByte.isValid(sValue))
            throw new NumberFormatException(
                    // Messages.getMessage("badUnsignedByte00") +
                    String.valueOf(sValue) + "]");
        lValue = new Long(sValue);
    }

    /**
     * validate the value against the xsd value space definition
     *
     * @param sValue number to check against range
     */
    public static boolean isValid(long sValue) {
        return !((sValue < 0L) || (sValue > 255L));
    }

}
