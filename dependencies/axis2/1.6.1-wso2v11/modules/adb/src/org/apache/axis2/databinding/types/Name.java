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


import org.apache.axis2.util.XMLChar;

/**
 * Custom class for supporting XSD data type Name Name represents XML Names. The value space of Name
 * is the set of all strings which match the Name production of [XML 1.0 (Second Edition)]. The base
 * type of Name is token.
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#Name">XML Schema 3.3.6</a>
 */
public class Name extends Token {

    private static final long serialVersionUID = -8354594301737358441L;

    public Name() {
        super();
    }

    /**
     * ctor for Name
     *
     * @throws IllegalArgumentException will be thrown if validation fails
     */
    public Name(String stValue) throws IllegalArgumentException {
        try {
            setValue(stValue);
        }
        catch (IllegalArgumentException e) {
            // recast normalizedString exception as token exception
            throw new IllegalArgumentException(
                    " invalid value for name " +
                    "data=[" + stValue + "]");
        }
    }

    /**
     * validates the data and sets the value for the object.
     *
     * @param stValue String value
     * @throws IllegalArgumentException if invalid format
     */
    public void setValue(String stValue) throws IllegalArgumentException {
        if (!Name.isValid(stValue))
            throw new IllegalArgumentException(
                    " invalid value for name " +
                    " data=[" + stValue + "]");
        m_value = stValue;
    }

    /**
     * validate the value against the xsd definition Name    ::=    (Letter | '_' | ':') (
     * NameChar)* NameChar    ::=     Letter | Digit | '.' | '-' | '_' | ':' | CombiningChar |
     * Extender
     */
    //todo - Fix this
    public static boolean isValid(String stValue) {
        int scan;
        boolean bValid = true;

        for (scan = 0; scan < stValue.length(); scan++) {
            if (scan == 0) {
                bValid = XMLChar.isNameStart(stValue.charAt(scan));
            } else {
                bValid = XMLChar.isName(stValue.charAt(scan));
            }
            if (!bValid)
                break;
        }

        return bValid;
    }
}
