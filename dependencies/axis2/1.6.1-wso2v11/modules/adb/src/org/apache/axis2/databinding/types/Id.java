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
 * Custom class for supporting XSD data type ID The base type of Id is NCName.
 *
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#ID">XML Schema 3.3.8</a>
 */
public class Id extends NCName {

    private static final long serialVersionUID = -8442438083214211437L;

    public Id() {
        super();
    }

    /**
     * Constructor for Id.
     *
     * @throws IllegalArgumentException will be thrown if validation fails
     */
    public Id(String stValue) throws IllegalArgumentException {
        try {
            setValue(stValue);
        }
        catch (IllegalArgumentException e) {
            // recast normalizedString exception as token exception
            throw new IllegalArgumentException();
            //Messages.getMessage("badIdType00") + "data=[" +
            //stValue + "]");
        }
    }

    /**
     * Validates the data and sets the value for the object.
     *
     * @param stValue String value
     * @throws IllegalArgumentException if invalid format
     */
    public void setValue(String stValue) throws IllegalArgumentException {
        if (!Id.isValid(stValue))
            throw new IllegalArgumentException(
                    // Messages.getMessage("badIdType00") +
                    " data=[" + stValue + "]");
        m_value = stValue;
    }

    /**
     * Validates the value against the xsd definition.
     * <p/>
     * Same validation as NCName for the time being
     */
    public static boolean isValid(String stValue) {
        return NCName.isValid(stValue);
    }
}
