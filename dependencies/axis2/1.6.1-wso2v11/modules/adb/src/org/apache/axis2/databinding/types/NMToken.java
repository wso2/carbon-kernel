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
 * Custom class for supporting XSD data type NMToken
 * <p/>
 * NMTOKEN represents the NMTOKEN attribute type from [XML 1.0(Second Edition)]. The value space of
 * NMTOKEN is the set of tokens that match the Nmtoken production in [XML 1.0 (Second Edition)]. The
 * base type of NMTOKEN is token.
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#nmtoken">XML Schema 3.3.4</a>
 */
public class NMToken extends Token {

    private static final long serialVersionUID = -1319741002733174329L;

    public NMToken() {
        super();
    }

    /**
     * ctor for NMToken
     *
     * @throws IllegalArgumentException will be thrown if validation fails
     */
    public NMToken(String stValue) throws IllegalArgumentException {
        try {
            setValue(stValue);
        }
        catch (IllegalArgumentException e) {
            // recast normalizedString exception as token exception
            throw new IllegalArgumentException(
                    //  Messages.getMessage("badNmtoken00") + "data=[" +
                    stValue + "]");
        }
    }

    /**
     * validate the value against the xsd definition Nmtoken    ::=    (NameChar)+ NameChar    ::=
     * Letter | Digit | '.' | '-' | '_' | ':' | CombiningChar | Extender
     */
    public static boolean isValid(String stValue) {
        int scan;

        for (scan = 0; scan < stValue.length(); scan++) {
            if (!XMLChar.isName(stValue.charAt(scan)))
                return false;
        }

        return true;
    }
}
