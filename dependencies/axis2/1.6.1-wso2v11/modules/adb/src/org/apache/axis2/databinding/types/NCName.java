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
 * Custom class for supporting XSD data type NCName NCName represents XML "non-colonized" Names The
 * base type of NCName is Name.
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#NCName">XML Schema 3.3.7</a>
 * @see <A href="http://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCName Production</a>
 */
public class NCName extends Name {

    private static final long serialVersionUID = 8573451628276898297L;

    public NCName() {
        super();
    }

    /**
     * ctor for NCName
     *
     * @throws IllegalArgumentException will be thrown if validation fails
     */
    public NCName(String stValue) throws IllegalArgumentException {
        try {
            setValue(stValue);
        }
        catch (IllegalArgumentException e) {
            // recast normalizedString exception as token exception
            throw new IllegalArgumentException(
                    // Messages.getMessage("badNCNameType00") +
                    "data=[" +
                            stValue + "]");
        }
    }

    /**
     * validates the data and sets the value for the object.
     *
     * @param stValue String value
     * @throws IllegalArgumentException if invalid format
     */
    public void setValue(String stValue) throws IllegalArgumentException {
        if (!NCName.isValid(stValue))
            throw new IllegalArgumentException(
                    //Messages.getMessage("badNCNameType00") +
                    " data=[" + stValue + "]");
        m_value = stValue;
    }

    /**
     * validate the value against the xsd definition
     * <p/>
     * NCName ::=  (Letter | '_') (NCNameChar)* NCNameChar ::=  Letter | Digit | '.' | '-' | '_' |
     * CombiningChar | Extender
     */
    public static boolean isValid(String stValue) {
        int scan;
        boolean bValid = true;

        for (scan = 0; scan < stValue.length(); scan++) {
            if (scan == 0)
                bValid = XMLChar.isNCNameStart(stValue.charAt(scan));
            else
                bValid = XMLChar.isNCName(stValue.charAt(scan));
            if (!bValid)
                break;
        }
        return bValid;
    }
}
