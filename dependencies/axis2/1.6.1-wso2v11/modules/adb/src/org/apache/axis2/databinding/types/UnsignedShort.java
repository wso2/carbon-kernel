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
 * Custom class for supporting primitive XSD data type UnsignedShort
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#unsignedShort">XML Schema 3.3.23</a>
 */
public class UnsignedShort extends UnsignedInt {

    private static final long serialVersionUID = 6822598447692816380L;

    public UnsignedShort() {

    }

    /**
     * ctor for UnsignedShort
     *
     * @throws NumberFormatException will be thrown if validation fails
     */
    public UnsignedShort(long sValue) throws NumberFormatException {
        setValue(sValue);
    }

    public UnsignedShort(String sValue) throws NumberFormatException {
        setValue(Long.parseLong(sValue));
    }

    /**
     * validates the data and sets the value for the object.
     *
     * @param sValue value
     */
    public void setValue(long sValue) throws NumberFormatException {
        if (!UnsignedShort.isValid(sValue))
            throw new NumberFormatException(
                    // Messages.getMessage("badUnsignedShort00") +
                    String.valueOf(sValue) + "]");
        lValue = new Long(sValue);
    }

    /** validate the value against the xsd definition */
    public static boolean isValid(long sValue) {
        return !((sValue < 0L) || (sValue > 65535L));
    }

}
