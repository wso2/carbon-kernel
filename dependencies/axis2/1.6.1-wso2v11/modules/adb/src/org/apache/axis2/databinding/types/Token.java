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
 * Custom class for supporting primitive XSD data type Token. token represents tokenized strings.
 * The base type of token is normalizedString.
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#token">XML Schema 3.3.2</a>
 */
public class Token extends NormalizedString {

    private static final long serialVersionUID = -7370524740514465467L;

    public Token() {
        super();
    }

    /**
     * ctor for Token
     *
     * @throws IllegalArgumentException will be thrown if validation fails
     */
    public Token(String stValue) throws IllegalArgumentException {
        try {
            setValue(stValue);
        }
        catch (IllegalArgumentException e) {
            // recast normalizedString exception as token exception
            throw new IllegalArgumentException(
                    //Messages.getMessage("badToken00") +
                    "data=[" + stValue + "]");
        }
    }

    /**
     * validate the value against the xsd definition
     * <p/>
     * The value space of token is the set of strings that do not contain the line feed (#xA) nor
     * tab (#x9) characters, that have no leading or trailing spaces (#x20) and that have no
     * internal sequences of two or more spaces. The lexical space of token is the set of strings
     * that do not contain the line feed (#xA) nor tab (#x9) characters, that have no leading or
     * trailing spaces (#x20) and that have no internal sequences of two or more spaces.
     */
    public static boolean isValid(String stValue) {
        int scan;
        // check to see if we have a string to review
        if ((stValue == null) || (stValue.length() == 0))
            return true;

        // no leading space
        if (stValue.charAt(0) == 0x20)
            return false;

        // no trail space
        if (stValue.charAt(stValue.length() - 1) == 0x20)
            return false;

        for (scan = 0; scan < stValue.length(); scan++) {
            char cDigit = stValue.charAt(scan);
            switch (cDigit) {
                case 0x09:
                case 0x0A:
                    return false;
                case 0x20:
                    // no doublspace
                    if (scan + 1 < stValue.length())
                        if (stValue.charAt(scan + 1) == 0x20) {
                            return false;
                        }
                default:
                    break;
            }
        }
        return true;
    }

    /**
     * validates the data and sets the value for the object.
     *
     * @param stValue String value
     * @throws IllegalArgumentException if invalid format
     */
    public void setValue(String stValue) throws IllegalArgumentException {
        if (!Token.isValid(stValue))
            throw new IllegalArgumentException(
                    //Messages.getMessage("badToken00") +
                    " data=[" + stValue + "]");
        m_value = stValue;
    }

}
